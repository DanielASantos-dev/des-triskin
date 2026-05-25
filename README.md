# Triskin

App Android nativo para operadores agrícolas da Data Agrin acompanharem tarefas
do dia, registrarem atividades de campo e consultarem o clima — com pleno
funcionamento offline.

## Stack

- **Kotlin 2.1** com **Jetpack Compose** (Material 3, BOM 2024.12)
- **Arquitetura**: Clean Architecture em três camadas (`domain`, `data`,
  `presentation`) com MVVM + Intent/State (MVI leve)
- **Injeção de dependência**: Hilt
- **Persistência local**: Room 2.6 (com migration explícita 1 → 2)
- **Rede**: Retrofit + Moshi + OkHttp (logging em debug)
- **Clima**: [Open-Meteo](https://open-meteo.com/) (sem chave) +
  [Geocoding API](https://open-meteo.com/en/docs/geocoding-api)
- **Localização**: Google Play Services Location + Android Geocoder
- **Coroutines / Flow** para reatividade
- **Testes**: JUnit 4, MockK, Turbine, kotlinx-coroutines-test
- **Lint**: ktlint

## Funcionalidades

### 1. Tarefas do dia (offline)
- Lista filtrada por **Hoje / Semana / Todas**, e adicionalmente por status e prioridade
- Nome da atividade, talhão/área, hora prevista, prioridade e status
- Troca rápida de status (Pendente → Em andamento → Finalizada) via chip clicável
- Criação via diálogo com `TimePicker`
- Persistência local obrigatória em Room

### 2. Registro de atividades (offline)
- Formulário com tipo (Plantio, Colheita, Adubação, Irrigação, Pulverização, Outro),
  talhão, hora de início / fim e observações
- **Vínculo a múltiplas tarefas**: dropdown multi-select de tarefas em aberto;
  ao registrar a atividade, as tarefas vinculadas são automaticamente marcadas
  como Finalizadas (cascade idempotente — tarefas já Finalizadas são ignoradas).
  Para progresso parcial, o operador atualiza o status na própria tarefa via o
  chip antes de registrar a atividade conclusiva.
- Edição: toque no card do histórico abre o form pré-preenchido, com as
  tarefas vinculadas já visíveis (mesmo que tenham sido finalizadas pelo
  cascade anterior)
- Histórico em aba dedicada com confirmação para exclusão
- Persistência local obrigatória em Room

### 3. Clima (online + cache)
- **Localização automática por padrão** via `FusedLocationProviderClient` +
  reverse geocoding (`android.location.Geocoder`) com permissão runtime
  `ACCESS_COARSE_LOCATION`. Na primeira abertura, o app pede permissão; se
  concedida, busca a localização atual e o clima dela.
- Busca manual de cidade com debounce (350 ms) via Open-Meteo Geocoding,
  acessível a qualquer momento (ícone de "minha localização" e campo de busca
  sempre visíveis)
- Temperatura atual, sensação, umidade, vento, ícone climático e descrição
- **Previsão das próximas 24 horas** em linha rolável
- **Indicador de origem**: chip "Ao vivo" (verde) quando o último fetch veio da API,
  ou "Offline · atualizado em DD/MM HH:mm" (cinza) quando os dados são do cache
- Cache em Room (incluindo previsão horária serializada como JSON) com fallback
  automático em caso de falha de rede

## Diferenciais entregues

- Suporte a modo offline completo com fallback automático do clima
- Testes unitários cobrindo use cases, mappers, repositórios e ViewModels
- Animações Compose: `AnimatedContent` em transições de estado, `animateItem()`
  nas listas, `animateColorAsState` no chip de status
- UI responsiva (`WindowSizeClass`): em telas largas a `NavigationBar` vira
  `NavigationRail`
- Injeção de dependência com Hilt em toda a pilha
- Strings extraídas para `res/values/strings.xml` (i18n-ready)
- Migration de banco explícita 1 → 2 (sem `fallbackToDestructiveMigration`)

## Estrutura

```
app/src/main/java/br/com/triskin/
├── data/
│   ├── local/         entities, DAOs, converters, TriskinDatabase
│   ├── mapper/        TaskMapper, FieldActivityMapper, WeatherMapper, WeatherCacheCodec
│   ├── remote/        Retrofit services + DTOs Moshi
│   └── repository/    implementações dos repositórios
├── di/                DatabaseModule, NetworkModule, RepositoryModule
├── domain/
│   ├── model/         Task, FieldActivity, WeatherInfo, etc.
│   ├── repository/    interfaces
│   └── usecase/       casos de uso (1 caso = 1 classe)
└── presentation/
    ├── navigation/    grafo + bottom nav / rail
    ├── screen/
    │   ├── task/      Tarefas
    │   ├── activity/  Atividades
    │   └── weather/   Clima
    ├── theme/         cores, tipografia, TriskinTheme
    ├── util/          Formatters, Labels, weatherIcon
    └── MainActivity.kt
```

## Como rodar

### Pré-requisitos
- Android Studio Ladybug (2024.2) ou superior
- JDK 17
- Android SDK 35 (`compileSdk`/`targetSdk`), minSdk 26
- Emulador ou dispositivo com Android 8+

### Setup
1. Clone o repositório.
2. Abra a pasta `triskin/` no Android Studio. O IDE baixará automaticamente as
   dependências e completará os arquivos do Gradle Wrapper (se ainda não
   estiverem presentes — o repositório inclui `gradle-wrapper.properties`
   apontando para Gradle 8.11.1).
3. Selecione o app `app` e dispositivo, e clique em **Run** ▶.

Caso prefira linha de comando (com o wrapper gerado):

```bash
./gradlew assembleDebug      # build
./gradlew installDebug       # instala no device conectado
./gradlew test               # roda os testes unitários
./gradlew ktlintCheck        # verifica estilo
```

## Decisões técnicas

- **Open-Meteo** foi escolhido por ser gratuito, sem chave de API e estável, o
  que reduz fricção na entrega e permite o build funcionar para qualquer
  avaliador imediatamente.
- **BaaS de sincronização** (Firebase / Supabase) foi tratado como diferencial e
  ficou de fora do escopo desta entrega para não acoplar a um provedor externo
  que exigiria configuração e credenciais. O campo `isSynced` permanece no
  schema, deixando a arquitetura preparada para um próximo passo.
- **Forecast horário em vez de diário**: o PDF pede "previsão das próximas
  horas". A tela mostra as próximas 24 horas em uma `LazyRow` rolável.
- **Cache do clima como linha única**: padrão `id = 1` no Room. Mais simples
  que múltiplas localizações em cache e suficiente para o caso de uso (operador
  acompanha o talhão atual).
- **Migration explícita**: a versão 1 (sem `talhao`, sem `field_activities`,
  com schema antigo de `cached_weather`) sobe para a versão 2 sem perda de
  dados de tarefas.

## Limitações conhecidas

- Sem testes instrumentados (espaço para crescer com Robolectric ou Android
  Test Orchestrator em uma próxima iteração).
- Localização do clima é por busca de texto, não por GPS — escolha deliberada
  para evitar fluxo de permissão sensível e suportar o caso "operador troca
  entre talhões / cidades manualmente".
