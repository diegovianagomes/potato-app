# Potato Project

## Visão Geral

Este projeto é uma Prova de Conceito (POC) de um aplicativo Android para monitoramento simulado de dados fisiológicos. Ele demonstra a coleta, exibição e análise básica de dados como Frequência Cardíaca (FC/HR), Variabilidade da Frequência Cardíaca (VFC/HRV - representada por SDNN), Atividade Eletrodérmica (EDA) e Temperatura da Pele. O aplicativo utiliza Jetpack Compose para a UI e simula os dados internamente, incluindo diferentes estados fisiológicos para maior realismo. Ele também gera alertas baseados em padrões detectados nos dados simulados.

## Arquitetura

O aplicativo segue uma arquitetura em camadas para separação de responsabilidades.

```mermaid
graph TD
    subgraph UI["UI Layer (Jetpack Compose)"]
        DS[DashboardScreen]
        AS[AlertsScreen]
        SDC[SensorDataChart (MPAndroidChart)]
    end

    subgraph Data["Data Layer"]
        SR[SensorRepository]
        AR[AlertRepository]
        SM[SensorManager (Simulação)]
    end

    subgraph Model["Model Layer"]
        SD[SensorData (HR, SDNN, EDA, Temp, Mov)]
        A[Alert (ID, Timestamp, Título, Desc, Severidade)]
        SS[SimulationState (Enum: CALM, MILD_STRESS, HIGH_STRESS)]
    end

    subgraph Utils["Utilities"]
        ME[MathExtensions <br/> standardDeviation()]
    end

    %% Conexões
    SM -- Simula Estado --> SS
    SM -- Simula IBI baseado em Estado --> SM
    SM -- Calcula HR, SDNN de IBIs --> SD
    SM -- Simula EDA baseado em Estado --> SD
    SM -- Usa Cálculo --> ME
    SM -- Gera --> SD
    SM -- Fornece Dados --> SR

    SR -- Coleta/Armazena Histórico --> SD
    AR -- Coleta Dados --> SR
    AR -- Analisa Padrões --> SD
    AR -- Usa Modelo --> A
    AR -- Gera --> A

    DS -- Consome --> SR
    DS -- Navega para --> AS
    AS -- Consome --> AR
    SDC -- Visualiza --> SR

    %% Estilo
    classDef uiLayer fill:#4285F4,stroke:#333,stroke-width:1px;
    classDef dataLayer fill:#34A853,stroke:#333,stroke-width:1px;
    classDef model fill:#FBBC05,stroke:#333,stroke-width:1px;
    classDef utils fill:#EA4335,stroke:#333,stroke-width:1px;

    class DS,AS,SDC uiLayer;
    class SR,AR,SM dataLayer;
    class SD,A,SS model;
    class ME utils;
```

### Camadas
- UI Layer: Componentes de interface do usuário construídos com Jetpack Compose.
    - DashboardScreen: Tela principal exibindo dados atuais, controle de monitoramento e navegação. Inclui cards de sensores e o gráfico.
    - AlertsScreen: Tela que lista os alertas gerados.
    - SensorDataChart: Componente que utiliza MPAndroidChart (via AndroidView) para visualização gráfica do histórico de dados.
    - theme: Define o tema visual customizado "Ghibli" do aplicativo.
- Data Layer: Gerenciamento e fornecimento de dados.
  - SensorManager: Responsável pela simulação dos dados fisiológicos. Não utiliza sensores de hardware reais nesta versão. Simula estados (Calmo, Estresse Leve, Estresse Alto), gera Intervalos Interbatimentos (IBIs) baseados no estado atual, calcula HR e SDNN (VFC) a partir dos IBIs, e simula EDA também influenciada pelo estado. Gerencia as transições aleatórias entre estados.
  - SensorRepository: Coleta os SensorData gerados pelo SensorManager e mantém um histórico recente (últimos 100 pontos) em memória, expondo-o via StateFlow.
  - AlertRepository: Coleta o histórico de dados do SensorRepository e analisa os dados para detectar padrões predefinidos (ex: regra para Estresse Elevado baseada em HR, SDNN e EDA). Gera objetos Alert quando um padrão é detectado e as condições de cooldown são atendidas.
  - Model Layer: Estruturas de dados.
- SensorData: Modelo para os dados simulados (HR, HRV/SDNN, EDA, Temp, Movimento, Timestamp).
  - Alert: Modelo para alertas (ID, Timestamp, Título, Descrição, Severidade, SensorData associado).
  - SimulationState: Enum que define os diferentes estados fisiológicos simulados e seus parâmetros alvo (IBI médio, variação IBI, EDA alvo).
  - Utils: Utilitários gerais.
  - MathExtensions: Contém funções de extensão, como standardDeviation() para calcular o SDNN.

### Fluxo de Dados

1) O SensorManager mantém um currentState (CALM, MILD_STRESS, HIGH_STRESS) e o alterna aleatoriamente ao longo do tempo.
2) A cada "batimento" simulado, o SensorManager gera um novo Intervalo Interbatimento (IBI), tendendo à média e variabilidade definidas pelo currentState.
3) O SensorManager calcula a Frequência Cardíaca (HR) e o SDNN (VFC) com base na média e desvio padrão dos IBIs recentes (janela de 30s).
4) O SensorManager simula a Atividade Eletrodérmica (EDA), tendendo ao valor alvo definido pelo currentState. Temperatura e Movimento são simulados com variações aleatórias simples.
5) Um objeto SensorData contendo (HR, SDNN, EDA, Temp, Mov, Timestamp) é gerado e emitido.
6) O SensorRepository coleta e armazena o histórico recente desses SensorData.
7) O AlertRepository coleta o histórico do SensorRepository.
8) O AlertRepository analisa os dados recentes em busca de padrões (ex: HR alta + SDNN baixo + EDA alta por X segundos).
9) Se um padrão é detectado e o cooldown permite, o AlertRepository gera um Alert (para o padrão de Estresse Elevado, seleciona aleatoriamente uma de três mensagens predefinidas) e o adiciona à sua lista.
10) Os componentes da UI Layer (DashboardScreen, AlertsScreen, SensorDataChart) observam os StateFlows dos repositórios (SensorRepository, AlertRepository) para exibir os dados e alertas mais recentes.Simulação Detalhada
   
### Simulação Detalhada

- Estados: O SensorManager alterna entre CALM, MILD_STRESS, e HIGH_STRESS. Cada estado define:
  - targetMeanIBI: Influencia a frequência cardíaca média.
  - ibiVariationRange: Influencia a variabilidade (resultando em maior ou menor SDNN).
  - targetEda: Influencia o nível de atividade eletrodérmica simulada.
- IBI/HR/SDNN: A simulação gera IBIs que tendem suavemente à média do estado atual, com variação aleatória controlada. HR e SDNN são calculados a partir desses IBIs, não simulados diretamente.
- EDA: Simula a EDA tendendo suavemente ao alvo definido pelo estado atual, com ruído adicionado.
- Transições: O estado muda aleatoriamente a cada 30-90 segundos (configurável).

### Geração de Alertas
- Orientada a Dados: Os alertas são disparados pela análise dos dados simulados (HR, SDNN, EDA) no AlertRepository.
- Regra Exemplo (Estresse Elevado): Um alerta é gerado se HR > 95 bpm, SDNN < 30 ms, e EDA > 7.0 µS por 8 pontos de dados consecutivos.
- Cooldown: Um cooldown de 2 minutos impede alertas repetitivos para a mesma condição.
- Mensagens Aleatórias (POC): Para o alerta de Estresse Elevado detectado, uma das três mensagens predefinidas é escolhida aleatoriamente para demonstração.


### Stack
- Kotlin
- Jetpack Compose (UI Toolkit)
- Kotlin Coroutines & StateFlow (Programação Assíncrona e Reativa)
- Material 3 Design
- MPAndroidChart (Gráficos)
- Gradle (Build System)