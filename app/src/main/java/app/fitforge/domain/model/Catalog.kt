package app.fitforge.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SportsGymnastics
import androidx.compose.ui.graphics.vector.ImageVector

interface Coded {
    val code: String
    val label: String
}

inline fun <reified T> fromCode(code: String?, default: T): T where T : Enum<T>, T : Coded =
    enumValues<T>().firstOrNull { it.code == code } ?: default

enum class Category(override val code: String, override val label: String, val icon: ImageVector) : Coded {
    MUSCULACAO("musculacao", "Musculação", Icons.Rounded.FitnessCenter),
    CALISTENIA("calistenia", "Calistenia", Icons.Rounded.SportsGymnastics),
    CARDIO("cardio", "Cardio", Icons.Rounded.DirectionsRun),
    HIIT("hiit", "HIIT", Icons.Rounded.Bolt),
    MOBILIDADE("mobilidade", "Mobilidade", Icons.Rounded.SelfImprovement),
}

enum class Level(override val code: String, override val label: String, val bars: Int) : Coded {
    INICIANTE("iniciante", "Iniciante", 1),
    INTERMEDIARIO("intermediario", "Intermediário", 2),
    AVANCADO("avancado", "Avançado", 3),
}

enum class Equipment(override val code: String, override val label: String, val homeFriendly: Boolean) : Coded {
    NENHUM("nenhum", "Sem equipamento", true),
    HALTERES("halteres", "Halteres", true),
    BARRA_FIXA("barra_fixa", "Barra fixa", true),
    ELASTICO("elastico", "Elástico", true),
    KETTLEBELL("kettlebell", "Kettlebell", true),
    BANCO_CADEIRA("banco_cadeira", "Banco ou cadeira", true),
    CORDA("corda", "Corda de pular", true),
    COLCHONETE("colchonete", "Colchonete", true),
    BOLA_SUICA("bola_suica", "Bola suíça", true),
    BOLA_MEDICINAL("bola_medicinal", "Bola medicinal", true),
    ROLO("rolo", "Rolo de liberação", true),
    BARRA("barra", "Barra e anilhas", false),
    PARALELAS("paralelas", "Paralelas", false),
    MAQUINA("maquina", "Máquina", false),
    CABO("cabo", "Polia / cabo", false),
    OUTRO("outro", "Outro", false),
}

enum class Muscle(override val code: String, override val label: String, val group: MuscleGroup) : Coded {
    CHEST("chest", "Peito", MuscleGroup.PEITO),
    SHOULDERS("shoulders", "Ombros", MuscleGroup.OMBROS),
    TRICEPS("triceps", "Tríceps", MuscleGroup.BRACOS),
    BICEPS("biceps", "Bíceps", MuscleGroup.BRACOS),
    FOREARMS("forearms", "Antebraços", MuscleGroup.BRACOS),
    LATS("lats", "Dorsais", MuscleGroup.COSTAS),
    MIDDLE_BACK("middle back", "Meio das costas", MuscleGroup.COSTAS),
    LOWER_BACK("lower back", "Lombar", MuscleGroup.COSTAS),
    TRAPS("traps", "Trapézio", MuscleGroup.COSTAS),
    ABDOMINALS("abdominals", "Abdômen", MuscleGroup.CORE),
    QUADRICEPS("quadriceps", "Quadríceps", MuscleGroup.PERNAS),
    HAMSTRINGS("hamstrings", "Posteriores de coxa", MuscleGroup.PERNAS),
    GLUTES("glutes", "Glúteos", MuscleGroup.PERNAS),
    CALVES("calves", "Panturrilhas", MuscleGroup.PERNAS),
    ADDUCTORS("adductors", "Adutores", MuscleGroup.PERNAS),
    ABDUCTORS("abductors", "Abdutores", MuscleGroup.PERNAS),
    NECK("neck", "Pescoço", MuscleGroup.OMBROS),
}

enum class MuscleGroup(val label: String) {
    PEITO("Peito"), COSTAS("Costas"), OMBROS("Ombros"), BRACOS("Braços"), CORE("Core"), PERNAS("Pernas"),
}

enum class Place(val label: String) { CASA("Em casa"), ACADEMIA("Academia") }

fun muscleLabel(code: String): String = Muscle.entries.firstOrNull { it.code == code }?.label ?: code.replaceFirstChar { it.uppercase() }
