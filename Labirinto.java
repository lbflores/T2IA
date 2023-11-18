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
 * 3. Seleção (contar colisoes e escolher os 2 que tiverem menos) se fiver algum com 0 colisoes ele achou a saida
 * 4. Reprodução
 * 5. Mutação (só escolhe um dos filhos e substitui alguns "genes" de forma aleatória)
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Labirinto {

    static int tamVetor = 0;
    static int tamPopulacao = 10;

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

        System.out.println("População inicial: ");
        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < populacao[i].length; j++) {
                System.out.print(populacao[i][j] + " ");
            }
            System.out.println();
        }

        return null;
    }
    

    private static int[][] gerarPopulacao() {
        int[][] populacao = new int[tamPopulacao][tamVetor];
        Random random = new Random();

        for (int i = 0; i < populacao.length; i++) {
            for (int j = 0; j < populacao[i].length; j++) {
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
