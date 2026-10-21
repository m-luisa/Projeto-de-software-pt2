# Diagramas de Classes

## 1. Modelo de domínio (`model` e `factory`)

```mermaid
classDiagram
    direction TB

    class ConservationStatus {
        <<enumeration>>
        EXTINCT
        EXTINCT_IN_THE_WILD
        CRITICALLY_ENDANGERED
        ENDANGERED
        VULNERABLE
        NEAR_THREATENED
        LEAST_CONCERN
        DATA_DEFICIENT
        NOT_EVALUATED
        +normalizeAPI(rawString: String)$ ConservationStatus
        +emPortugues() String
    }

    class Species {
        <<abstract>>
        -id : int
        -scientificName : String
        -status : ConservationStatus
        -habitat : String
        -diet : String
        -length : String
        -mass : String
        +getId() int
        +getScientificName() String
        +getStatus() ConservationStatus
        +getHabitat() String
        +getDiet() String
        +getLength() String
        +getMass() String
        #appendCampo(sb: StringBuilder, rotulo: String, valor: String)$ void
        #describeCommonTraits() String
        +describeHabitat()* String
    }

    class Bird {
        +describeHabitat() String
    }
    class Plant {
        +describeHabitat() String
    }
    class Mammal {
        -behaviorPattern : String
        +describeHabitat() String
    }
    class Amphibian {
        +describeHabitat() String
    }
    class Reptile {
        +describeHabitat() String
    }
    class Insect {
        -wingType : String
        +describeHabitat() String
    }
    class Fish {
        +describeHabitat() String
    }
    class Mollusk {
        +describeHabitat() String
    }
    class Crustacean {
        +describeHabitat() String
    }
    class GenericSpecies {
        -taxonomicClass : String
        +getTaxonomicClass() String
        +describeHabitat() String
    }

    class Occurrence {
        -Occurrence(builder: OccurrenceBuilder)
        -date : Date
        -latitude : double
        -longitude : double
        -speciesRef : Species
        -horarioConhecido : boolean
        +setDate(d: Date) void
        +setCoordinates(lat: double, lon: double) void
        +setHorarioConhecido(horarioConhecido: boolean) void
        +getDate() Date
        +getLatitude() double
        +getLongitude() double
        +getSpeciesRef() Species
        +isHorarioConhecido() boolean
    }

    class OccurrenceBuilder {
        <<Occurrence.Builder>>
        +data(date: Date) OccurrenceBuilder
        +coordenadas(lat: double, lon: double) OccurrenceBuilder
        +especie(especie: Species) OccurrenceBuilder
        +horarioConhecido(conhecido: boolean) OccurrenceBuilder
        +build() Occurrence
    }

    class CriadorDeEspecie {
        <<abstract>>
        #criar(id: int, nome: String, status: ConservationStatus)* Species
        +criarDaAPI(id: int, nome: String, statusBruto: String) Species
    }

    class SearchResult {
        -key : int
        -scientificName : String
        -taxonomicClass : String
        -vernacularNames : List~String~
        +getKey() int
        +getScientificName() String
        +getTaxonomicClass() String
        +getVernacularNames() List~String~
    }

    class ConservationReport {
        -ConservationReport()
        +agruparPorStatus(especies: List~Species~)$ Map
        +formatar(especies: List~Species~)$ String
    }

    class SpeciesFactory {
        +createFromAPI(id: int, name: String, taxonomicClass: String, rawStatus: String)$ Species
    }

    Species <|-- Bird
    Species <|-- Plant
    Species <|-- Mammal
    Species <|-- Amphibian
    Species <|-- Reptile
    Species <|-- Insect
    Species <|-- Fish
    Species <|-- Mollusk
    Species <|-- Crustacean
    Species <|-- GenericSpecies

    Species --> ConservationStatus : status
    Occurrence --> Species : speciesRef

    OccurrenceBuilder ..> Occurrence : build
    Occurrence +-- OccurrenceBuilder : nested
    CriadorDeEspecie <|-- CriadorDeAve
    CriadorDeEspecie <|-- CriadorDeMamifero
    CriadorDeEspecie <|-- CriadorDeInseto
    CriadorDeEspecie <|-- CriadorDePlanta
    CriadorDeEspecie <|-- CriadorDeAnfibio
    CriadorDeEspecie <|-- CriadorDeReptil
    CriadorDeEspecie <|-- CriadorDePeixe
    CriadorDeEspecie <|-- CriadorDeMolusco
    CriadorDeEspecie <|-- CriadorDeCrustaceo
    CriadorDeEspecie <|-- CriadorDeEspecieGenerica
    CriadorDeEspecie ..> Species : factory method
    CriadorDeEspecie ..> ConservationStatus : normalizeAPI
    SpeciesFactory ..> CriadorDeEspecie : escolhe e delega
    ConservationReport ..> Species : agrupa
    ConservationReport ..> ConservationStatus : agrupa por
```

## 2. Camada de acesso a dados (`api`), fachada (`facade`) e `Main`

```mermaid
classDiagram
    direction LR

    class SpeciesDataSource {
        <<interface>>
        +fetchSpecies(speciesId: int) Species
        +fetchSpeciesSemEnriquecimento(speciesId: int) Species
        +searchByVernacular(termo: String, pageSize: int) SearchSession
        +searchByScientific(termo: String, pageSize: int) SearchSession
        +fetchRawOccurrencesByScientificName(scientificName: String, limit: int) List~String~
        +importarOcorrencias(jsonsBrutos: List~String~, especie: Species) ImportResult
    }

    class SearchSession {
        <<interface>>
        +currentPage() List~SearchResult~
        +pageIndex() int
        +hasResults() boolean
        +nextPage() boolean
        +previousPage() boolean
    }

    class GBIFApiClient {
        -IDIOMA_PADRAO : String$
        -LIMITE_NOMES_POPULARES : int$
        -client : HttpClient
        +fetchSpecies(speciesId: int) Species
        +fetchSpeciesSemEnriquecimento(speciesId: int) Species
        +searchByVernacular(termo: String, pageSize: int) SearchSession
        +searchByScientific(termo: String, pageSize: int) SearchSession
        +fetchRawOccurrencesByScientificName(scientificName: String, limit: int) List~String~
        +importarOcorrencias(jsonsBrutos: List~String~, especie: Species) ImportResult
    }

    class DecoradorDeFonteDeEspecies {
        <<abstract>>
        #fonte : SpeciesDataSource
        #DecoradorDeFonteDeEspecies(fonte: SpeciesDataSource)
        +fetchSpecies(speciesId: int) Species
        +fetchSpeciesSemEnriquecimento(speciesId: int) Species
        +searchByVernacular(termo: String, pageSize: int) SearchSession
        +searchByScientific(termo: String, pageSize: int) SearchSession
        +fetchRawOccurrencesByScientificName(scientificName: String, limit: int) List~String~
        +importarOcorrencias(jsonsBrutos: List~String~, especie: Species) ImportResult
    }

    class EnrichedSpeciesDataSource {
        -wikidata : WikiDataClient
        +fetchSpecies(speciesId: int) Species
        -enrich(base: Species, traits: WikiDataTraits) Species
    }

    class WikiDataClient {
        -client : HttpClient
        +fetchTraits(scientificName: String) WikiDataTraits
    }

    class WikiDataTraits {
        <<record>>
        habitat : String
        diet : String
        wingspan : String
        length : String
        mass : String
        dielCycle : String
        +vazio()$ WikiDataTraits
    }

    class GBIFSearchSession {
        -pageFetcher : IntFunction
        -pageSize : int
        -cachedPages : List
        -currentIndex : int
        -maybeHasMorePages : boolean
        +currentPage() List~SearchResult~
        +pageIndex() int
        +hasResults() boolean
        +nextPage() boolean
        +previousPage() boolean
    }

    class ImportResult {
        +ocorrencias : List~Occurrence~
        +descartados : int
    }

    class FachadaBiodiversidade {
        -TAMANHO_PAGINA_LOTE : int$
        -LIMITE_OCORRENCIAS_POR_ESPECIE : int$
        -fonte : SpeciesDataSource
        +buscarEspecies(termo: String, tamanhoPagina: int) SearchSession
        +buscarTodasAsCorrespondencias(termo: String) List~SearchResult~
        +obterEspecie(chave: int) Species
        +ocorrenciasDaEspecie(especie: Species) ImportResult
        +ocorrenciasDasCorrespondencias(correspondencias: List~SearchResult~) ImportResult
        +agruparPorStatus(especies: List~Species~) Map
    }

    class Main {
        +main(args: String[])$ void
    }

    class Species
    class Occurrence
    class OccurrenceBuilder
    class SearchResult
    class SpeciesFactory
    class ConservationReport

    SpeciesDataSource <|.. GBIFApiClient
    SpeciesDataSource <|.. DecoradorDeFonteDeEspecies
    DecoradorDeFonteDeEspecies <|-- EnrichedSpeciesDataSource
    SearchSession <|.. GBIFSearchSession

    DecoradorDeFonteDeEspecies o--> SpeciesDataSource : fonte
    EnrichedSpeciesDataSource o--> WikiDataClient : wikidata
    WikiDataClient ..> WikiDataTraits : creates
    EnrichedSpeciesDataSource ..> WikiDataTraits : usa

    GBIFApiClient ..> GBIFSearchSession : creates
    GBIFApiClient ..> SpeciesFactory : usa
    GBIFApiClient ..> OccurrenceBuilder : monta via builder

    SpeciesDataSource ..> SearchSession
    SpeciesDataSource ..> ImportResult
    SpeciesDataSource ..> Species
    SearchSession ..> SearchResult
    ImportResult o--> Occurrence : ocorrencias

    FachadaBiodiversidade o--> SpeciesDataSource : fonte
    FachadaBiodiversidade ..> ConservationReport : agrupa
    FachadaBiodiversidade ..> SearchSession
    FachadaBiodiversidade ..> ImportResult

    Main ..> FachadaBiodiversidade : usa
    Main ..> SpeciesDataSource
    Main ..> EnrichedSpeciesDataSource : instancia
    Main ..> GBIFApiClient : instancia
    Main ..> WikiDataClient : instancia
```


---

# Padrões Criacionais aplicados

Atividade: identificar onde padrões criacionais devem ser aplicados e aplicá-los, justificando aqui no README. Nesta entrega foram aplicados o **Factory Method** (criação de `Species`) e o **Builder** (criação de `Occurrence`).

| Padrão | Onde | Sinal de alerta que havia no código |
|---|---|---|
| Factory Method | `factory/CriadorDeEspecie` e `CriadorDe*`, usados por `SpeciesFactory` | `switch` escolhendo qual classe instanciar |
| Builder | `Occurrence.Builder`, usado em `GBIFApiClient.importarOcorrencias` | Construtor com valores posicionais e criação em dois passos (construtor + setter) |

## 1. Factory Method: criação de `Species`

**Antes.** `SpeciesFactory.createFromAPI` tinha um `switch` com um `case` por classe taxonômica, e cada `case` fazia `new Bird(...)`, `new Mammal(...)` e assim por diante. Uma categoria nova exigia editar esse `switch`.

**Agora.** `CriadorDeEspecie` (Creator) declara o método de fábrica `criar(...)`, e cada categoria tem seu criador concreto (`CriadorDeAve`, `CriadorDeMamifero`...). `SpeciesFactory` apenas escolhe o criador num `Map` e delega. O método `criarDaAPI` da classe base concentra o passo comum (normalizar o status da API) em um único lugar.

**Por quê.** Segundo os slides, o sinal de alerta do Factory Method é um `if/else` crescente escolhendo qual classe instanciar. O RF4 pede pelo menos quatro categorias e o RF6 exige que adicionar uma nova categoria não mude o código do relatório. O Factory Method estende essa mesma ideia à criação: uma categoria nova é um novo criador mais uma linha no mapa. A assinatura de `createFromAPI` foi mantida, então o `GBIFApiClient` não mudou nessa parte.

**Custo.** São 10 classes pequenas. Isso só compensa porque há várias variantes (9 categorias mais o caso genérico); com um único tipo de produto o padrão seria exagero.

## 2. Builder: criação de `Occurrence`

**Antes.** `new Occurrence(data, lat, lon, especie)` seguido de `occ.setHorarioConhecido(...)`. Eram dois passos (esquecer o segundo gerava um valor errado em silêncio) e dois `double` seguidos que o compilador não deixa distinguir.

**Agora.** O construtor é privado e o objeto é montado por `new Occurrence.Builder().especie(...).data(...).coordenadas(...).horarioConhecido(...).build()`. Todas as validações rodam no `build()`.

**Por quê.** O RF2 exige que valores fisicamente impossíveis (data no futuro, longitude 400°) nunca sejam armazenados. Com o construtor privado, não existe `Occurrence` sem passar pela validação. Cada etapa é nomeada, o que elimina a confusão entre latitude e longitude. O Builder guarda coordenadas como `Double` para distinguir "não informado" (`null`) de uma coordenada válida `0.0, 0.0`.

**Onde não foi aplicado.** `Species` também tem construtores longos, mas só é construída dentro dos criadores, que são código interno. Um Builder ali acrescentaria código sem ganho perceptível de legibilidade.

**Observação sobre os diagramas.** No diagrama, `OccurrenceBuilder` representa a classe aninhada `Occurrence.Builder`, porque o Mermaid não aceita ponto no nome de uma classe.

## Requisitos funcionais preservados

Os padrões não alteram o comportamento observável das consultas, ocorrências e relatórios. As validações de `Species` (RF1) e `Occurrence` (RF2), a normalização de status (RF3), as descrições por categoria (RF4 e RF5), a paginação sob demanda (RF7 e RF10) e o descarte tolerante de registros inválidos em lote (RF9) continuam com o mesmo comportamento.

---

# Padrões Estruturais aplicados

Atividade: identificar onde padrões estruturais (Adapter, Decorator, Proxy, Facade e Composite) devem ser aplicados e aplicá-los, justificando aqui no README.

## Facade: `FachadaBiodiversidade`

| Onde | Sinal de alerta que havia no código |
|---|---|
| `facade/FachadaBiodiversidade`, usada pelo `Main` | O `Main` combinava à mão quatro peças do subsistema (busca, paginação, ocorrências, relatório); a regra "popular, senão científico" estava escrita três vezes |

**Antes.** O `Main` misturava interface com o usuário (menus, impressão) e orquestração do subsistema. A regra "tenta o nome popular e, se não houver resultado, tenta o científico" aparecia em `resolverEspecie`, `resolverEspecies` e `listarOcorrenciasPorNome`. O fluxo "para cada espécie encontrada, buscar ocorrências brutas e converter" e o método `collectAllPages` também viviam no `Main`.

**Agora.** A fachada expõe poucos métodos com nomes de intenção: `buscarEspecies`, `buscarTodasAsCorrespondencias`, `obterEspecie`, `ocorrenciasDaEspecie`, `ocorrenciasDasCorrespondencias` e `agruparPorStatus`. O `Main` só pergunta ao usuário, chama a fachada e imprime. Foram movidos para a fachada: o fallback popular → científico (três cópias viraram uma), `collectAllPages`, o laço por espécie do fluxo de ocorrências e as constantes de processamento em lote (`TAMANHO_PAGINA_LOTE`, `LIMITE_OCORRENCIAS_POR_ESPECIE`). O tamanho da página de tela (`PAGINA`) continua no `Main`, por ser decisão de apresentação.

O `Main` continua sendo o único lugar que instancia as classes concretas, e entrega a fonte de dados à fachada:

```java
SpeciesDataSource fonte = new EnrichedSpeciesDataSource(new GBIFApiClient(), new WikiDataClient());
FachadaBiodiversidade fachada = new FachadaBiodiversidade(fonte);
```

**Por quê.** O Facade serve para dar uma interface simples a um subsistema com várias peças. Aqui o ganho é concreto: a regra de fallback agora muda em um lugar só, e o `Main` deixa de saber como `SpeciesDataSource`, `SearchSession`, `ImportResult` e `ConservationReport` se combinam. A fachada recebe a fonte pelo construtor e depende só da interface `SpeciesDataSource` (RF7 [IF]).

**Custo.** Uma classe e um nível de indireção a mais. A fachada não bloqueia o subsistema (as classes continuam públicas) e `SearchSession` e `ImportResult` continuam sendo devolvidos como tipos de retorno, porque a fachada simplifica o uso e não replica esses tipos.

**Requisitos funcionais preservados.** O contrato `SpeciesDataSource` não mudou, e os fluxos e as mensagens das opções 1, 2 e 3 do menu continuam os mesmos. As validações de `Species` (RF1) e `Occurrence` (RF2), a normalização de status (RF3), as descrições por categoria (RF4 e RF5) e o relatório agnóstico a tipo (RF6) não foram tocados. A paginação sob demanda (RF7 e RF10) continua nas `SearchSession`, que a fachada apenas repassa, e o descarte tolerante de registros inválidos (RF9) continua em `importarOcorrencias`.

## Decorator: `DecoradorDeFonteDeEspecies` e `EnrichedSpeciesDataSource`

| Onde | Sinal de alerta que havia no código |
|---|---|
| `api/DecoradorDeFonteDeEspecies` (base) e `EnrichedSpeciesDataSource` | Já existia um "embrulho" de `SpeciesDataSource`, mas ele reescrevia à mão cinco métodos que só repassavam a chamada; um segundo embrulho repetiria tudo |

| Papel no padrão | Classe |
|---|---|
| Component | `SpeciesDataSource` |
| ConcreteComponent | `GBIFApiClient` |
| Decorator | `DecoradorDeFonteDeEspecies` (implementa a interface **e** guarda outra `SpeciesDataSource`) |
| ConcreteDecorator | `EnrichedSpeciesDataSource` |

**Antes.** `EnrichedSpeciesDataSource` já implementava `SpeciesDataSource` e guardava um `gbif`, ou seja, já era um decorador na prática. Mas não havia classe base: ele escrevia à mão `searchByVernacular`, `searchByScientific`, `fetchRawOccurrencesByScientificName`, `importarOcorrencias` e `fetchSpeciesSemEnriquecimento`, que só repassavam a chamada.

**Agora.** `DecoradorDeFonteDeEspecies` concentra o repasse de todos os métodos para a fonte "de dentro". `EnrichedSpeciesDataSource` herda dela e sobrescreve apenas `fetchSpecies`, o único método cujo resultado muda (busca os traits no Wikidata e reconstrói a espécie). `fetchSpeciesSemEnriquecimento` passou a ser herdado: ele pede à fonte de dentro a versão sem enriquecimento (antes chamava `gbif.fetchSpecies`). Para o `GBIFApiClient` o resultado é o mesmo, e a nova forma continua correta se houver outro decorador no meio da pilha. O construtor manteve a mesma assinatura, então o `Main` não precisou mudar:

```java
SpeciesDataSource client = new EnrichedSpeciesDataSource(new GBIFApiClient(), new WikiDataClient());
```

**Por quê.** O Decorator acrescenta uma responsabilidade (enriquecer com Wikidata) sem alterar o `GBIFApiClient` nem quem usa a interface. Como o decorador também é um `SpeciesDataSource`, ele pode ser empilhado com outros e trocado na montagem do `Main`, sem `if` espalhado no código.

**Custo.** Uma classe abstrata a mais. Hoje existe um único decorador concreto, então o ganho imediato é remover o código de repasse repetido e deixar o `EnrichedSpeciesDataSource` focado só no que ele muda; a classe base passa a compensar de fato quando surgir um segundo decorador (por exemplo, um que registre logs ou guarde respostas). Uma mudança de comportamento mínima: passar uma fonte `null` ao construtor agora lança `IllegalArgumentException` na hora, em vez de falhar com `NullPointerException` só no primeiro uso.

**Requisitos funcionais preservados.** O contrato `SpeciesDataSource` não ganhou nem perdeu métodos, e o `Main`, o modelo e o relatório não foram alterados. As buscas, a paginação sob demanda (RF7 e RF10), a importação com descarte tolerante de registros inválidos (RF9) e o enriquecimento da espécie continuam se comportando como antes; `fetchSpeciesSemEnriquecimento` continua sem consultar o Wikidata.