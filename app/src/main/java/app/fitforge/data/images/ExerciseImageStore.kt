package app.fitforge.data.images

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseImageStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val dir = File(context.filesDir, "exercise-images").apply { mkdirs() }
    private val client by lazy { OkHttpClient() }

    fun fileFor(url: String): File {
        val hash = MessageDigest.getInstance("SHA-1").digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(dir, "$hash.jpg")
    }

    fun resolve(url: String): Any {
        if (!url.startsWith("http")) return url
        val f = fileFor(url)
        return if (f.exists() && f.length() > 0) f else url
    }

    fun isStored(url: String): Boolean = !url.startsWith("http") || fileFor(url).exists()

    suspend fun download(urls: Collection<String>, onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }): Int =
        withContext(Dispatchers.IO) {
            val pending = urls.filter { it.startsWith("http") && !fileFor(it).exists() }.distinct()
            val done = AtomicInteger(0)
            val ok = AtomicInteger(0)
            val gate = Semaphore(6)
            onProgress(0, pending.size)
            coroutineScope {
                pending.map { url ->
                    async {
                        gate.withPermit {
                            if (fetch(url)) ok.incrementAndGet()
                            onProgress(done.incrementAndGet(), pending.size)
                        }
                    }
                }.awaitAll()
            }
            ok.get()
        }

    private fun fetch(url: String): Boolean = runCatching {
        client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val target = fileFor(url)
            val tmp = File(target.path + ".tmp")
            val body = resp.body ?: return false
            body.byteStream().use { input -> tmp.outputStream().use { input.copyTo(it) } }
            tmp.renameTo(target)
        }
    }.getOrDefault(false)

    fun storedBytes(): Long = dir.listFiles()?.sumOf { it.length() } ?: 0L

    fun storedCount(): Int = dir.listFiles()?.size ?: 0

    suspend fun importFromGallery(uri: Uri, exerciseId: String): String = withContext(Dispatchers.IO) {
        val custom = File(context.filesDir, "custom-images").apply { mkdirs() }
        val target = File(custom, "${exerciseId}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
        Uri.fromFile(target).toString()
    }
}
