# FitForge

App Android pessoal de treino, focado em **constância** e em **emagrecer**, com treinos de academia e também de casa (calistenia, peso corporal, HIIT). Tudo offline: sem login, sem servidor, os dados ficam no celular.

> Uso pessoal, não está na Play Store.

## O que já tem

| Fase | Conteúdo | Status |
|---|---|---|
| 1 | Projeto base, tema escuro/claro, navegação, banco com **924 exercícios** (243 em português), biblioteca com busca e filtros, detalhe do exercício, exercícios personalizados com foto e vídeo | ✅ |
| 2 | Montador de treinos, treinos prontos, modo treino com timer (inclusive de tela bloqueada), Tabata/EMOM, registro básico das sessões | ⏳ |
| 3 | Calendário, sequência (streak), meta semanal, gráficos, recordes, peso e medidas | ⏳ |
| 4 | Onboarding, gerador de plano, lembretes, backup em JSON, polimento visual | ⏳ |

## Stack

Kotlin · Jetpack Compose · Material 3 · MVVM · Hilt · Coroutines/Flow · Room · DataStore · Navigation Compose (rotas tipadas) · Coil 3 · Vico.
As animações (confete, chama do streak, estados vazios) são feitas em Compose nativo.

- `minSdk` 26 (Android 8), `targetSdk`/`compileSdk` 37
- AGP 9.5 · Kotlin 2.4 · Gradle 9.9 · JDK 17

## Estrutura

```
app/src/main/
├─ assets/exercises.json        base de exercícios já normalizada (gerada pelo script)
└─ java/app/fitforge/
   ├─ data/      db (Room), seed, prefs (DataStore), repo, images (cache offline)
   ├─ domain/    modelos e regras (categorias, músculos, equipamentos…)
   ├─ di/        módulos Hilt
   └─ ui/        theme, components, navigation, screens/*
data-src/                       fontes da base de exercícios
├─ free-exercise-db.json        dataset original (domínio público)
├─ traducao/*.json              nome e instruções em português
└─ extras.json                  exercícios extras de casa/calistenia/HIIT
tools/build_exercises.py        junta tudo e gera o assets/exercises.json
```

### Base de exercícios

A base vem do [free-exercise-db](https://github.com/yuhonas/free-exercise-db) (domínio público). Cada exercício tem duas fotos (início e fim do movimento), que o app alterna em loop para parecer uma animação. As imagens são carregadas da URL raw do GitHub e ficam em cache. As dos exercícios usados nos treinos são baixadas para dentro do app, para funcionar sem internet na academia. Em **Perfil → Imagens offline** dá para baixar a biblioteca inteira.

Para mudar traduções ou extras, edite os arquivos em `data-src/`, rode `python3 tools/build_exercises.py` e aumente `SEED_VERSION` no script. Assim o app atualiza a base na próxima abertura, mantendo favoritos e exercícios próprios.

## Compilar

Pré-requisitos: JDK 17 e Android SDK (variável `ANDROID_HOME` ou `local.properties` com `sdk.dir=...`).

```bash
./gradlew assembleDebug
# APK em app/build/outputs/apk/debug/app-debug.apk
```

Com o celular ligado no USB (depuração USB ativa):

```bash
./gradlew installDebug
```

## Instalar o APK pelo GitHub

1. Cada push na `main` dispara o workflow **APK debug** (aba *Actions*).
2. Abra a execução mais recente e baixe o artifact `FitForge-AAAAMMDD-xxxxxxx` (é um `.zip` com o `app-debug.apk`).
3. No celular, abra o APK e permita "instalar apps desconhecidos" para o navegador ou gerenciador de arquivos.

O APK do GitHub e o compilado localmente usam a **mesma chave de debug** (`app/debug.keystore`, versionada de propósito). Por isso um instala por cima do outro sem perder os dados.

## Licenças

- Código: uso pessoal.
- Exercícios e imagens: free-exercise-db, Unlicense (domínio público).
- Fonte Sora: SIL Open Font License.
