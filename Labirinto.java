/*
 * T2 - Labirinto
 */

 /*
    encontrar um caminho da entrada até a saída, não precisa ser o mais curto.
    fazer carga de arquivo 12X12
    função heurística
    visualização: com algortimo genético é preciso mostrar a população sendo gerada e aptidões evoluindo
  */

/*
 * TODO
 * 
 * 1. Leitura do arquivo -> OK
 * 2. População inicial -> OK
 * 3. Seleção (seleciona 2 com melhor pontuação) -> Parcial, faz a seleção de 2 vetores com melhor pontuação mas não descarta vetores que iniciam errado ou erram muito no inicio
 * 4. Reprodução
 * 5. Mutação (só escolhe um dos filhos e substitui alguns "genes" de forma aleatória)
 * 6. Calcular aptidão
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Labirinto {

    static int tamVetor = 0;
    static int tamPopulacao = 5;

    //Penalidade ao repetir caminho
    static final int PEN_REPETIU = -1;
    
    // Penalidade ao colidir com parede
    static final int PEN_COLIDIU = -2;
    
    // Penalidade ao sair do labirinto    
    static final int PEN_SAIU = -3;

    // Pontua ao acertar caminho
    static final int PON_ACERTOU = 2;

    // Pontua ao achar saida
    static final int PON_ACHOU = 5;

    public static void main(String[] args) {
        char[][] labirinto = lerLabirinto("labirinto1.txt");      

        System.out.println("Labiritno original");
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                System.out.print(labirinto[i][j] + " ");
                if(labirinto[i][j] == '0') tamVetor++;
            }
            System.out.println();
        }
        tamVetor += 2;

        System.out.println("Tamanho de cada vetor da população: " + tamVetor);

        int[] caminho = algoritmoGenetico(labirinto);       

        // Imprimir o caminho atualizado
        System.out.println("Confere o caminho percorrido:");
        for (int i = 0; i < caminho.length; i++) {
            System.out.print(caminho[i] + " ");
        }
        System.out.println();

        // Imprimir o labirinto com o caminho final
        System.out.println("Labirinto com o caminho percorrido:");
        // O vetor caminhoNaMatriz tem os movimentos feitos pelo algoritmo genético
        // sendo 0 para cima, 1 para baixo, 2 para esquerda, 3 para direita.Saindo sempre de [0,0] do labirinto.

        char[][] caminhoNaMatriz = caminhoNaMatriz(labirinto, caminho);

        for (int i = 0; i < caminhoNaMatriz.length; i++) {
            for (int j = 0; j < caminhoNaMatriz[i].length; j++) {
                System.out.print(caminhoNaMatriz[i][j] + " ");
                
            }
            System.out.println();
        }
    }

    static class Posicao {
        public int linha;
        public int coluna;
        
        public Posicao( int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
        }
    }

    private static void calculaPontuacao(int[][] populacao, char[][] labirinto) {
        Posicao posicaoAtual = new Posicao(0, 0);
        
        for (int i = 0; i < populacao.length; i++) {
            ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
            for (int j = 0; j < populacao[i].length-2; j++) {
                // Caminha pelo labirinto
                caminhaNoLabirinto(populacao[i][j], posicaoAtual);
                // Altera pontuação
                populacao[i][populacao[i].length-2] += buscaPontuacao(posicaoAtual, caminhoPercorrido, labirinto);
            }
        }
    }

    private static void caminhaNoLabirinto(int movimento, Posicao posicaoAtual) {
        switch (movimento) {
            case 0:
                // Anda para cima
                posicaoAtual.linha += -1;
                break;
            case 1:
                // Anda para baixo
                posicaoAtual.linha += 1;
                break;
            case 2:
                // Anda para esquerda
                posicaoAtual.coluna += -1;                        
                break;
            case 3: 
                // Anda para direita
                posicaoAtual.coluna =+ 1;
                break;
            default:
                break;
        }
    }

    private static int buscaPontuacao(Posicao posicaoAtual, ArrayList<Posicao> caminhoPercorrido, char[][] labirinto) {
        int pontuacao = 0;
        try {                    
            switch (labirinto[posicaoAtual.linha][posicaoAtual.coluna]) {
                case '0':
                    pontuacao += PON_ACERTOU;
                    break;
                case '1':
                    pontuacao += PEN_COLIDIU;
                    break;
                case '3':
                    pontuacao += PON_ACHOU;
                    break;
                default:                            
                    break;
            }

            for (Posicao p : caminhoPercorrido) {
                if(p.linha == posicaoAtual.linha && p.coluna == posicaoAtual.coluna) {
                    pontuacao +=  PEN_REPETIU;
                }
            }
            caminhoPercorrido.add(posicaoAtual);              
            return pontuacao;
        } catch (ArrayIndexOutOfBoundsException e){
            pontuacao += PEN_SAIU;
            return pontuacao;
        } 
    }

    // valida o caminho guardando no último espaço do vetor o ponto em que esta certo
    private static void validaCaminho(char[][] labirinto, int[][] populacao) {
        for (int i = 0; i < populacao.length; i++) {
            populacao[i][tamVetor-1] = buscaQuantMovimentoValido(i, populacao, labirinto);
        }
    }

    private static int buscaQuantMovimentoValido(int linhaAtual,int[][] populacao, char[][] labirinto ) {
        int quantMovimentos = 0;
        Posicao posicaoAtual = new Posicao(0, 0);

        for (int j = 0; j < populacao[linhaAtual].length-2; j++) {
            caminhaNoLabirinto(populacao[linhaAtual][j], posicaoAtual);
            try {
                // se o movimento é correto add mais um
                if(labirinto[posicaoAtual.linha][posicaoAtual.coluna] == '0' 
                    || labirinto[posicaoAtual.linha][posicaoAtual.coluna] == '3' ) {
                    quantMovimentos+= 1;
                } else {
                    return quantMovimentos;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return quantMovimentos;
            }
        }
        return quantMovimentos;
    }

    private static char[][] caminhoNaMatriz(char[][] labirinto, int[] caminho) {
        char[][] caminhoMatriz = Arrays.stream(labirinto)
                .map(char[]::clone)
                .toArray(char[][]::new);
    
        for (int i = 0; i < caminho.length; i++) {
            int linha = caminho[i] / labirinto.length;
            int coluna = caminho[i] % labirinto.length;
    
            // Verifica se a posição gerada está dentro dos limites da matriz
            if (linha >= 0 && linha < labirinto.length && coluna >= 0 && coluna < labirinto[0].length) {
                char seta;
                switch (caminho[i]) {
                    case 0:
                        seta = '^'; // Seta para cima
                        break;
                    case 1:
                        seta = 'v'; // Seta para baixo
                        break;
                    case 2:
                        seta = '<'; // Seta para a esquerda
                        break;
                    case 3:
                        seta = '>'; // Seta para a direita
                        break;
                    default:
                        seta = 'X'; // Caso não seja um movimento válido
                }
    
                caminhoMatriz[linha][coluna] = seta;
            }
        }
        return caminhoMatriz;
    }

    private static int[] algoritmoGenetico(char[][] labirinto) {
        int[][] populacaoAtual = gerarPopulacao();
        int[][] populacaoIntermediaria = gerarPopulacao();

        System.out.println("População inicial");
        printPopulacao(populacaoAtual);

        calculaPontuacao(populacaoAtual, labirinto);
        validaCaminho(labirinto, populacaoAtual);

        System.out.println("População inicial com Pontuação calculada: ");
        printPopulacao(populacaoAtual);       

        // selção por elitismo
        System.out.println("Elitismo: ");
        int[] selecionado = elitismo(populacaoAtual);
        for (int i = 0; i < tamVetor; i++) {
            System.out.print(selecionado[i] + " ");
        }
        System.out.println();
        // coloca o selecionado por elitismo para proxima geração
        for (int i = 0; i < tamVetor; i++) {
            populacaoIntermediaria[0][i] = selecionado[i];
        }

        // Faz seleção por torneio para selecionar pai e mãe
        for (int i = 1; i < populacaoIntermediaria.length; i+=2) { // a partir de 1 para manter a linha do elitismo
            int indicePai = torneio(populacaoAtual, labirinto);
            int indiceMae = torneio(populacaoAtual, labirinto);
            
            //fazer crossover usando mascara binária    
            int[][] filhos = crossover(populacaoAtual[indicePai], populacaoAtual[indiceMae]);

            populacaoIntermediaria[i] = filhos[0]; // Primeiro filho
            populacaoIntermediaria[i + 1] = filhos[1]; // Segundo filho           

            // // Avalia o filho 1
            // int pontuacaoFilho1 = funcHeuristica(populacaoIntermediaria[i], labirinto);
            // System.out.println("Pontuação do filho 1: " + pontuacaoFilho1);
            // // Avalia o filho 2
            // int pontuacaoFilho2 = funcHeuristica(populacaoIntermediaria[i + 1], labirinto);
            // System.out.println("Pontuação do filho 2: " + pontuacaoFilho2);
            
            // //Aplica mutação
            // populacaoIntermediaria[i] = mutacao(populacaoIntermediaria[i], labirinto);
            // populacaoIntermediaria[i + 1] = mutacao(populacaoIntermediaria[i + 1], labirinto);               
            
        }

        return null;
    }

    private static int[][] crossover(int[] pai, int[] mae) {
        Random random = new Random();
        int corte = pai[tamVetor-1];
        if(corte < mae[tamVetor-1]){
            corte = mae[tamVetor-1];
        }
    
        // máscara binária
        int[] mascaraBinaria = new int[tamVetor];
        for (int i = 0; i < tamVetor; i++) {
            mascaraBinaria[i] = random.nextInt(2); // 0 ou 1
        }
    
        // Aplica a máscara para gerar o primeiro filho
        int[] filho1 = new int[tamVetor];
        for (int i = 0; i < tamVetor; i++) {
            if (i < pai[tamVetor-1]) {
                filho1[i] = pai[i];
            } else {
                // Se for 1 na máscara, herda do pai, senão, herda da mãe
                filho1[i] = (mascaraBinaria[i] == 1) ? pai[i] : mae[i];
            }
        }

        // Aplica a máscara invertida para gerar o segundo filho
        int[] filho2 = new int[tamVetor];
        for (int i = 0; i < tamVetor; i++) {
            // Se for 0 na máscara, herda do pai, senão, herda da mãe
            filho2[i] = (mascaraBinaria[i] == 0) ? pai[i] : mae[i];
        }

        // retornar vetor com os dois filhos
        return new int[][] { filho1, filho2 };
    }

    private static int[] elitismo(int[][] populacao) {
        int[] selecionado = new int[tamVetor];
        selecionado[tamVetor-2] = -1000000;

        for (int i = 0; i< populacao.length; i++) {          
            if (populacao[i][populacao[i].length-2] >= selecionado[tamVetor-2]) {
                for (int j = 0; j < tamVetor; j++) {
                    selecionado[j] = populacao[i][j];
                }
            }
        }
        return selecionado;
    }

    private static int torneio(int[][] populacaoAtual, char[][] labirinto) {
        Random random = new Random();
    
        // Seleciona dois indivíduos aleatórios
        int indice1 = random.nextInt(populacaoAtual.length);
        int indice2 = random.nextInt(populacaoAtual.length);
    
        // Avalia os dois indivíduos no torneio
        int pontuacao1 = populacaoAtual[indice1][tamVetor-2];
        int pontuacao2 = populacaoAtual[indice2][tamVetor-2];
    
        // Retorna o índice do indivíduo com maior pontuação
        return (pontuacao1 > pontuacao2) ? indice1 : indice2;
    }
    
    private static void printPopulacao(int[][] populacao) {
        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < populacao[i].length; j++) {
                System.out.print(populacao[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static int[][] gerarPopulacao() {
        //+1 para guardar a pontuação na ultima posição do vetor
        int[][] populacao = new int[tamPopulacao][tamVetor];
        Random random = new Random();

        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < populacao[i].length-2; j++) {
                populacao[i][j] = random.nextInt(4);             
            }
        }        
        return populacao;
    }



    public static char[][] lerLabirinto(String nomeArquivo) {
        char[][] labirinto = new char[12][12];

        try (BufferedReader br = new BufferedReader(new FileReader(nomeArquivo))) {
            String linha;
            int linhaAtual = 0;

            while ((linha = br.readLine()) != null) {
                String[] valores = linha.split(" ");

                for (int i = 0; i < valores.length; i++) {
                    if (valores[i].equals("E") || valores[i].equals("S")) {
                        // Mapear "E" para 2 (entrada) e "S" para 3 (saída)
                        labirinto[linhaAtual][i] = valores[i].equals("E") ? '2' : '3';
                    } else {
                        labirinto[linhaAtual][i] = valores[i].charAt(0);
                    }
                }

                linhaAtual++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return labirinto;
    }

    
}
