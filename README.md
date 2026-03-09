# CalcFX — Calculadora Científica

Calculadora científica desktop desenvolvida com **JavaFX 21**, seguindo a arquitetura **MVC** e avaliação de expressões via algoritmo **Shunting-Yard** de Dijkstra.

## Funcionalidades

### Operações básicas
- Adição, subtração, multiplicação e divisão
- Porcentagem (`%`)
- Troca de sinal (`+/-`)
- Backspace e limpeza (`C`)

### Operações científicas
| Função | Descrição |
|--------|-----------|
| `sin`, `cos`, `tan` | Funções trigonométricas |
| `asin`, `acos`, `atan` | Funções trigonométricas inversas |
| `log` | Logaritmo na base 10 |
| `ln` | Logaritmo natural |
| `√` | Raiz quadrada |
| `x²` | Quadrado |
| `xʸ` | Potenciação |
| `exp` | Exponencial (eˣ) |
| `1/x` | Inverso |
| `n!` | Fatorial |
| `π`, `e` | Constantes matemáticas |

### Modos de ângulo
- **RAD** — radianos
- **DEG** — graus

### Expressões compostas
- Suporte a parênteses com balanceamento automático
- Multiplicação implícita: `5(3+2)` → `5×(3+2)`
- Encadeamento de operações após `=`

## Tecnologias

- Java 21
- JavaFX 21.0.2
- Maven
- JUnit 5.10 + Mockito 5.8 (testes)

## Estrutura do projeto

```
calcfx/
├── src/
│   ├── main/
│   │   ├── java/com/calcfx/
│   │   │   ├── App.java                        # Ponto de entrada JavaFX
│   │   │   ├── controller/
│   │   │   │   └── CalculatorController.java   # Liga View ao Service (MVC)
│   │   │   ├── model/
│   │   │   │   └── CalculatorModel.java        # Estado da calculadora
│   │   │   ├── service/
│   │   │   │   └── CalculatorService.java      # Lógica de negócio
│   │   │   └── util/
│   │   │       ├── ExpressionEvaluator.java    # Shunting-Yard + avaliação
│   │   │       └── MathUtil.java               # Utilitários matemáticos
│   │   └── resources/com/calcfx/
│   │       ├── view/calculator.fxml            # Layout da interface
│   │       └── css/style.css                   # Estilos
│   └── test/
│       └── java/com/calcfx/service/
│           └── CalculatorServiceTest.java      # Testes unitários
└── pom.xml
```

## Como executar

**Pré-requisitos:** Java 21+ e Maven instalados.

```bash
# Executar a aplicação
mvn javafx:run

# Rodar os testes
mvn test

# Compilar sem executar
mvn compile
```

## Arquitetura

O projeto segue o padrão **MVC**:

- **Model** (`CalculatorModel`) — mantém o estado: expressão acumulada, entrada pendente e modo de ângulo.
- **View** (`calculator.fxml` + `style.css`) — interface declarativa em FXML.
- **Controller** (`CalculatorController`) — recebe eventos da UI e delega ao `CalculatorService`, sem lógica de negócio.
- **Service** (`CalculatorService`) — orquestra as operações e aciona o avaliador de expressões.
- **ExpressionEvaluator** — tokeniza, converte para notação pós-fixa (Shunting-Yard) e avalia a expressão.
