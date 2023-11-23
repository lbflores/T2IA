/*
 *  Labirinto com algoritmo genético
 *  Objetivo: encontrar caminho que leva a saída S, não necessáriamente o caminho mais curto.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class LabirintoAG {

    //Penalidade ao repetir caminho
    static final int PEN_REPETIU = -20;
    
    // Penalidade ao colidir com parede
    static final int PEN_COLIDIU = -60;
    
    // Penalidade ao sair do labirinto    
    static final int PEN_SAIU = -70;

    // Pontua ao acertar caminho
    static final int PON_ACERTOU = 10;

    // Pontua ao achar saida
    static final int PON_ACHOU = 20;

    // Pontua ao achar saida
    static final int PEN_CAMINHOU = -2;

    static int iteracao = 0;

    static boolean achou = false;



    public static void main(String[] args) {
        char[][] labirinto = lerLabirinto("labirinto_teste1.txt");
        int espacosLivres = contarEspacosLivres(labirinto);
        //cria vetor de 11 X espaços livres, significa 11 possíveis soluções
        int[][] populacaoAtual = new int[5][espacosLivres];
        int[][] populacaoIntermediaria = new int[5][espacosLivres];
        iteracao = 0;
        int maxIteracoes = 10000; 

        System.out.println("Labiritno original");
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                System.out.print(labirinto[i][j] + " ");
            }
            System.out.println();
        }
        inicializaPopulacao(populacaoAtual);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("log.txt"))) {
            StringBuilder sb;
       
                while (!solucaoEncontrada(populacaoAtual, labirinto) && iteracao < maxIteracoes) {

                    if(iteracao == 500) {
                        System.out.println("500");
                        System.out.println();
                    }
                    
                    //Função heurística
                    //imprime população atual
                    System.out.println("População atual:");
                    for (int i = 0; i < populacaoAtual.length; i++) {
                        for (int j = 0; j < populacaoAtual[i].length; j++) {
                            System.out.print(populacaoAtual[i][j] + " ");
                        }
                        System.out.println();
                    }
                    avaliaPopulação(populacaoAtual, labirinto);

                    // Algoritmo Genético
                    // Faz seleção por elitismo
                    elitismo(populacaoAtual, populacaoIntermediaria, labirinto);

                    // Faz seleção por torneio para selecionar pai e mãe
                    for (int i = 1; i < populacaoIntermediaria.length; i+=2) { // a partir de 1 para manter a linha do elitismo
                        int indicePai = torneio(populacaoAtual, labirinto);
                        int indiceMae = torneio(populacaoAtual, labirinto);
                        
                        int[][] filhos = crossover(populacaoAtual[indicePai], populacaoAtual[indiceMae], labirinto);
            
                        populacaoIntermediaria[i] = filhos[0]; // Primeiro filho
                        populacaoIntermediaria[i + 1] = filhos[1]; // Segundo filho
                        //fazer crossover usando mascara binária    

                        // Avalia o filho 1
                        int pontuacaoFilho1 = funcHeuristica(populacaoIntermediaria[i], labirinto);
                        //System.out.println("Pontuação do filho 1: " + pontuacaoFilho1);
                        // Avalia o filho 2
                        int pontuacaoFilho2 = funcHeuristica(populacaoIntermediaria[i + 1], labirinto);
                    // System.out.println("Pontuação do filho 2: " + pontuacaoFilho2);
                        
                        //Aplica mutação
                        populacaoIntermediaria[i] = mutacao(populacaoIntermediaria[i], labirinto);
                        populacaoIntermediaria[i + 1] = mutacao(populacaoIntermediaria[i + 1], labirinto);
                    }

                    // Atualiza a população atual com a população intermediária
                    for (int i = 0; i < populacaoAtual.length; i++) {
                        populacaoAtual[i] = Arrays.copyOf(populacaoIntermediaria[i], populacaoIntermediaria[i].length);
                    }
                    iteracao++;

                    // Escreve LOG completo
                    sb = new StringBuilder();
                    bw.write("----------------------------------------------------------------------------------");
                    bw.newLine();
                    bw.write("GERAÇÃO -> " + iteracao + "\n");
                    for (int i = 0; i < populacaoAtual.length; i++) {
                        for (int p : populacaoAtual[i]) {
                            sb.append(p).append(" ");
                        }
                        bw.write(sb.toString());
                        bw.newLine();
                        sb = new StringBuilder();
                    }
                    bw.write("PONTUAÇÕES ");
                    bw.newLine();
                    for (int i = 0; i < populacaoAtual.length; i++) {
                        int pontuacao = funcHeuristica(populacaoAtual[i], labirinto);
                        bw.write("Pontuação do indivíduo " + i + ": " + pontuacao);
                        bw.newLine();
                    }
                }

                // Imprimir o caminho atualizado ou mensagem de falha
                if (solucaoEncontrada(populacaoAtual, labirinto)) {
                    System.out.println("Solução encontrada na iteração " + iteracao + ":");
                    System.out.println();
                } else {
                    System.out.println("Nenhuma solução encontrada após " + maxIteracoes + " iterações.");

                    System.out.println("Caminhos realizados:");

                    System.out.println();

                    int count = 0;
                    for(int[] individuo: populacaoAtual){
                        System.out.println("Indivíduo " + count);
                        imprimirCaminhoEncontrado(individuo, labirinto);
                        count++;
                        System.out.println();
                    }
                    

                    System.out.println();
                }   

                
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private static boolean solucaoEncontrada(int[][] populacaoAtual, char[][] labirinto) {
        for (int[] individuo : populacaoAtual) {
            funcHeuristica(individuo, labirinto);
            if (achou) {
                //imprimi individuo
                System.out.println("Solução encontrada:");
                for (int i = 0; i < individuo.length; i++) {
                    System.out.print(individuo[i] + " ");
                }
                System.out.println();
                imprimirCaminhoEncontrado(individuo, labirinto);
                System.out.println();
                return true;
            }
        }
        return false;
    }

    private static void imprimirCaminhoEncontrado(int[] individuo, char[][] labirinto) {
        char[][] labirintoCaminho = new char[labirinto.length][labirinto[0].length];

        // Copia o labirinto original para o labirinto do caminho
        for (int i = 0; i < labirinto.length; i++) {
            labirintoCaminho[i] = Arrays.copyOf(labirinto[i], labirinto[i].length);
        }
    
        int posicaoX = -1;
        int posicaoY = -1;
    
        // Encontra a posição inicial (entrada)
        for (int i = 0; i < labirintoCaminho.length; i++) {
            for (int j = 0; j < labirintoCaminho[i].length; j++) {
                if (labirintoCaminho[i][j] == '2') {
                    posicaoX = i;
                    posicaoY = j;
                    break;
                }
            }
            if (posicaoX != -1) {
                break;
            }
        }
    
        // Percorre o caminho e marca no labirinto
        for (int movimento : individuo) {
            switch (movimento) {
                case 0: // Cima
                    posicaoX--;
                    break;
                case 1: // Baixo
                    posicaoX++;
                    break;
                case 2: // Esquerda
                    posicaoY--;
                    break;
                case 3: // Direita
                    posicaoY++;
                    break;
            }
    
            // Marca o caminho no labirinto
            if (posicaoX >= 0 && posicaoX < labirintoCaminho.length && posicaoY >= 0 && posicaoY < labirintoCaminho[0].length) {
                if (labirintoCaminho[posicaoX][posicaoY] != '2' && labirintoCaminho[posicaoX][posicaoY] != 'S') {
                    labirintoCaminho[posicaoX][posicaoY] = '#';
                }
            }
        }
    
        // Imprime os labirintos lado a lado
        for (int i = 0; i < labirintoCaminho.length; i++) {
            // Imprime o labirinto original
            for (int j = 0; j < labirinto[i].length; j++) {
                System.out.print(labirinto[i][j] + " ");
            }
    
            // Adiciona espaço entre os labirintos
            System.out.print("  ");
    
            // Imprime o labirinto com o caminho
            for (int j = 0; j < labirintoCaminho[i].length; j++) {
                System.out.print(labirintoCaminho[i][j] + " ");
            }
    
            System.out.println();
        }
    }



    private static int[] mutacao(int[] linhaAtual, char[][] labirinto) {
        Random random = new Random();
     

        
        int corte = buscaPontoDeCorte(linhaAtual, labirinto);
        int dispovinelParaMutar = linhaAtual.length - corte;
        int quantidadeMutacao =  (int) (dispovinelParaMutar * 0.5);

  
        int count = 0;

        int[] melhorMutacao = Arrays.copyOf(linhaAtual, linhaAtual.length);
        int melhorPontuacao = funcHeuristica(linhaAtual, labirinto);
        //System.out.println("Melhor pontuação antes da mutação: " + melhorPontuacao);

        int[] mutacao = Arrays.copyOf(linhaAtual, linhaAtual.length);

        if (quantidadeMutacao > 0) {
            while (count <= quantidadeMutacao) {           
                // Escolhe randomicamente um movimento para mutar
                int indiceMovimentoMutado = random.nextInt(corte, mutacao.length);

                // Substitui o movimento por outro aleatório
                mutacao[indiceMovimentoMutado] = random.nextInt(4); // 0 a 3

                count++;
            }
        }

         // Retorna a melhor mutação
        melhorMutacao = Arrays.copyOf(mutacao, mutacao.length);
        //System.out.println("Melhor pontuação após a mutação: " + melhorPontuacao);
        return melhorMutacao;
    }



    private static int[][] crossover(int[] pai, int[] mae, char[][] labirinto) {
        Random random = new Random();

        int corte = buscaPontoDeCorte(pai, labirinto);        
        int corteMae = buscaPontoDeCorte(mae, labirinto);

        int[] melhorCorte = Arrays.copyOf(pai, pai.length);
        if(corte < corteMae) {
            corte = corteMae;
            melhorCorte = Arrays.copyOf(mae, mae.length);
        }

        int tamanho = pai.length;
    
        // máscara binária
        int[] mascaraBinaria = new int[tamanho];
        for (int i = 0; i < tamanho; i++) {
            mascaraBinaria[i] = random.nextInt(2); // 0 ou 1
        }
    
        // Aplica a máscara para gerar o primeiro filho
        int[] filho1 = new int[tamanho];
        for (int i = 0; i < tamanho - 2; i++) {
            // Se for 1 na máscara, herda do pai, senão, herda da mãe
            if (i < corte){
                filho1[i] = melhorCorte[i];
            } else {
                filho1[i] = (mascaraBinaria[i] == 1) ? pai[i] : mae[i];
            }            
        }

        // Aplica a máscara invertida para gerar o segundo filho
        int[] filho2 = new int[tamanho];
        for (int i = 0; i < tamanho - 2; i++) {
            if (i < corte){
                filho2[i] = melhorCorte[i];
            } else {
                // Se for 0 na máscara, herda do pai, senão, herda da mãe
                filho2[i] = (mascaraBinaria[i] == 0) ? pai[i] : mae[i];
            }            
        }

        // retornar vetor com os dois filhos
        return new int[][] { filho1, filho2 };
    }

    static class Posicao {
        public int linha;
        public int coluna;

        public Posicao( int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
        }
    }

    private static int buscaPontoDeCorte(int[] populacaoAtual, char[][] labirinto) {
        int corte = 0;
        int posicaoAtualX = -1;
        int posicaoAtualY = -1;
        ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
        caminhoPercorrido.add(new Posicao(0 , 0));

        // Encontra a posição inicial (entrada)
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                if (labirinto[i][j] == '2') {
                    posicaoAtualX = i;
                    posicaoAtualY = j;
                    break;
                }
            }
            if (posicaoAtualX != -1) {
                break;
            }
        }

        // Percorre o caminho e atualiza a pontuação
        for(int i = 0; i < populacaoAtual.length; i++) {        
  
            switch (populacaoAtual[i]) {
                case 0: // Cima
                    posicaoAtualX--;
                    break;
                case 1: // Baixo
                    posicaoAtualX++;
                    break;
                case 2: // Esquerda
                    posicaoAtualY--;
                    break;
                case 3: // Direita
                    posicaoAtualY++;
                    break;
            }

            // Verifica as condições e atribui pontuações e penalidades
            if (posicaoAtualX < 0 || posicaoAtualX >= labirinto.length || posicaoAtualY < 0 || posicaoAtualY >= labirinto[0].length) {
                // SAIU
                return corte;
            } else if (labirinto[posicaoAtualX][posicaoAtualY] == '1') {
                // COLIDIU
                return corte;
            } else if (labirinto[posicaoAtualX][posicaoAtualY] == '3') {
                // ACHOU
                achou = true;
                return corte = i;
            } else {
                for (Posicao p : caminhoPercorrido) {
                    if(p.linha == posicaoAtualX && p.coluna == posicaoAtualY) {
                        //contRep++;
                        if (corte - 2 < 0) {
                            return 0;
                        }
                        return corte - 2;
                    }
                }
                caminhoPercorrido.add(new Posicao(posicaoAtualX , posicaoAtualY));

                corte = i + 1;
            }
        }
        return corte;
    }

    private static int torneio(int[][] populacaoAtual, char[][] labirinto) {
        Random random = new Random();
    
        // Seleciona dois indivíduos aleatórios
        int indice1 = random.nextInt(populacaoAtual.length);
        int indice2 = random.nextInt(populacaoAtual.length);
     

        int pontuacao1 = funcHeuristica(populacaoAtual[indice1], labirinto);
        int pontuacao2 = funcHeuristica(populacaoAtual[indice1], labirinto);

    
        // Retorna o índice do indivíduo com maior pontuação
        return (pontuacao1 > pontuacao2) ? indice1 : indice2;
    }

    private static void elitismo(int[][] populacaoAtual, int[][] populacaoIntermediaria, char[][] labirinto) {
        // Encontrar o indivíduo mais apto na população atual
        int melhorIndividuo = -1;
        int melhorPontuacao = Integer.MIN_VALUE;
    
        for (int i = 0; i < populacaoAtual.length; i++) {
            int pontuacao = funcHeuristica(populacaoAtual[i], labirinto);
            if (pontuacao > melhorPontuacao) {
                melhorPontuacao = pontuacao;
                melhorIndividuo = i;
            }       
            
        }

    }
   
    private static void avaliaPopulação(int[][] populacaoAtual, char[][] labirinto) {
        for (int i = 0; i < populacaoAtual.length; i++) {
            int pontuacao = funcHeuristica(populacaoAtual[i], labirinto);
            System.out.println("Pontuação do indivíduo " + i + ": " + pontuacao);
        }
    }


    private static int funcHeuristica(int[] populacaoAtual, char[][] labirinto) {
        int pontuacao = 0;
        int posicaoAtualX = -1;
        int posicaoAtualY = -1;
        ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
        caminhoPercorrido.add(new Posicao(0 , 0));

        // Encontra a posição inicial (entrada)
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                if (labirinto[i][j] == '2') {
                    posicaoAtualX = i;
                    posicaoAtualY = j;
                    break;
                }
            }
            if (posicaoAtualX != -1) {
                break;
            }
        }

        // Percorre o caminho e atualiza a pontuação
        for (int movimento : populacaoAtual) {
            switch (movimento) {
                case 0: // Cima
                    posicaoAtualX--;
                    break;
                case 1: // Baixo
                    posicaoAtualX++;
                    break;
                case 2: // Esquerda
                    posicaoAtualY--;
                    break;
                case 3: // Direita
                    posicaoAtualY++;
                    break;
            }

            // Verifica as condições e atribui pontuações e penalidades
            if (posicaoAtualX < 0 || posicaoAtualX >= labirinto.length || posicaoAtualY < 0 || posicaoAtualY >= labirinto[0].length) {
                pontuacao += PEN_SAIU;
                return pontuacao;
            } else if (labirinto[posicaoAtualX][posicaoAtualY] == '1') {
                pontuacao += PEN_COLIDIU;
                return pontuacao;
            } else if (labirinto[posicaoAtualX][posicaoAtualY] == '3') {
                pontuacao = PON_ACHOU;
                achou = true;
                return pontuacao;
            } else {
                // Verifica se a posição já foi visitada
                for (Posicao p : caminhoPercorrido) {
                    if(p.linha == posicaoAtualX && p.coluna == posicaoAtualY) {
                        pontuacao += PEN_REPETIU;
                    }
                }
                caminhoPercorrido.add(new Posicao(posicaoAtualX , posicaoAtualY));
                pontuacao += PON_ACERTOU;
                pontuacao += PEN_CAMINHOU;
            }
    }

    return pontuacao;
    }


    private static void inicializaPopulacao(int[][] populacaoAtual) {
        Random random = new Random();
        for (int i = 0; i < populacaoAtual.length; i++) {
            for (int j = 0; j < populacaoAtual[i].length; j++) {
                populacaoAtual[i][j] = random.nextInt(4);
            }
        }
    }

    private static int contarEspacosLivres(char[][] labirinto) {
        int espacosLivres = 0;
        for (int i = 0; i < labirinto.length; i++) {
            for (int j = 0; j < labirinto[i].length; j++) {
                if (labirinto[i][j] == '0') {
                    espacosLivres++;
                }
            }
        }
        //soma 10% de espaços livres
        espacosLivres += (int) (espacosLivres * 0.1);

        return espacosLivres;
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