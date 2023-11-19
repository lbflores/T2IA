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

    static int tamVetor = 10;
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
                //if(labirinto[i][j] == '0') tamVetor++;
            }
            System.out.println();
        }

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
        int linha = 0;
        int coluna = 0;       
        
        for (int i = 0; i < populacao.length; i++) {
            ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
            for (int j = 0; j < populacao[i].length-1; j++) {
                // Caminha pelo labirinto
                switch (populacao[i][j]) {
                    case 0:
                        // Anda para cima
                        linha =+ -1;                            
                        break;
                    case 1:
                        // Anda para baixo
                        linha =+ 1;
                        break;
                    case 2:
                        // Anda para esquerda
                        coluna =+ -1;
                        
                        break;
                    case 3: 
                        // Anda para direita
                        coluna =+ 1;
                        break;
                    default:
                }
                // Altera pontuação
                try {                    
                    switch (labirinto[linha][coluna]) {
                        case '0':
                            populacao[i][populacao[i].length-1] += PON_ACERTOU;
                            
                            break;
                        case '1':
                            populacao[i][populacao[i].length-1] += PEN_COLIDIU;
                            break;
                        case '3':
                            populacao[i][populacao[i].length-1] += PON_ACHOU;
                            break;
                        default:                            
                            break;                    
                    }

                    for (Posicao p : caminhoPercorrido) {
                        if(p.linha == linha && p.coluna == coluna) {
                            populacao[i][populacao[i].length-1] += PEN_REPETIU;
                        }
                    }
                    caminhoPercorrido.add(new Posicao(linha, coluna));              
         
                } catch (ArrayIndexOutOfBoundsException e){
                    populacao[i][populacao[i].length-1] += PEN_SAIU;
                }   
            }
        }
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
        int[][] populacao = gerarPopulacao();

        System.out.println("População inicial");
        printPopulacao(populacao);

        calculaPontuacao(populacao, labirinto);

        System.out.println("População inicial com Pontuação calculada: ");
        printPopulacao(populacao);       

        // selção
        System.out.println("selecionados: ");
        ArrayList<int[]> selecionados = selecao(populacao);
        for (int[] s : selecionados) {
            for(int i = 0; i < tamVetor+1; i++){
                System.out.print(s[i] + " ");
            }
            System.out.println();
        }

        return null;
    }

    private static ArrayList<int[]> selecao(int[][] populacao) {
        int pontuacao1 = -1000000000;
        int pontuacao2 = -1000000000;
        int[] selecionado1 = new int[tamVetor + 1];
        int[] selecionado2 = new int[tamVetor + 1];
        ArrayList<int[]>selecionados = new ArrayList<>();

        for(int i = 0; i< populacao.length; i++) {
            if (populacao[i][populacao[i].length-1] > pontuacao1) {
                pontuacao1 = populacao[i][populacao[i].length-1];
                for(int j = 0; j < tamVetor+1; j++) {
                    selecionado1[j] = populacao[i][j];
                }
            } 
            else if (populacao[i][populacao[i].length-1] > pontuacao2) {
                pontuacao2 = populacao[i][populacao[i].length-1];
                for(int j = 0; j < tamVetor+1; j++) {
                    selecionado2[j] = populacao[i][j];
                }
            }
        }
        selecionados.add(selecionado1);
        selecionados.add(selecionado2);
        return selecionados;
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
        int[][] populacao = new int[tamPopulacao][tamVetor+1];
        Random random = new Random();

        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < populacao[i].length-1; j++) {
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
