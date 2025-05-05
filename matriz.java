class Matriz {
    private Celula inicio;
    private int linha, coluna;
 
    public Matriz() {
       this(3, 3);
    }
 
    public Matriz(int linha, int coluna) {
       this.linha = linha;
       this.coluna = coluna;
 
       // Alocação da matriz encadeada
       Celula[] linhas = new Celula[linha];
       for (int i = 0; i < linha; i++) {
          Celula atual = new Celula();
          if (i == 0) {
             inicio = atual;
          }
          linhas[i] = atual;
 
          Celula anterior = atual;
          for (int j = 1; j < coluna; j++) {
             Celula nova = new Celula();
             anterior.dir = nova;
             nova.esq = anterior;
             anterior = nova;
          }
       }
 
       // Ligar as linhas verticalmente
       for (int i = 0; i < linha - 1; i++) {
          Celula sup = linhas[i];
          Celula inf = new Celula();
          linhas[i + 1] = inf;
 
          for (int j = 0; j < coluna; j++) {
             if (j > 0) inf = inf.esq.dir;
             sup.inf = inf;
             inf.sup = sup;
             sup = sup.dir;
          }
       }
    }
 
    public Matriz soma(Matriz m) {
       Matriz resp = null;
 
       if (this.linha == m.linha && this.coluna == m.coluna) {
          resp = new Matriz(this.linha, this.coluna);
          Celula aLinha = this.inicio;
          Celula bLinha = m.inicio;
          Celula cLinha = resp.inicio;
 
          for (int i = 0; i < linha; i++) {
             Celula a = aLinha;
             Celula b = bLinha;
             Celula c = cLinha;
             for (int j = 0; j < coluna; j++) {
                c.elemento = a.elemento + b.elemento;
                a = a.dir;
                b = b.dir;
                c = c.dir;
             }
             aLinha = aLinha.inf;
             bLinha = bLinha.inf;
             cLinha = cLinha.inf;
          }
       }
 
       return resp;
    }
 
    public Matriz multiplicacao(Matriz m) {
       Matriz resp = null;
 
       if (this.coluna == m.linha) {
          resp = new Matriz(this.linha, m.coluna);
 
          Celula linhaA = this.inicio;
          Celula linhaResp = resp.inicio;
 
          for (int i = 0; i < this.linha; i++) {
             Celula colunaResp = linhaResp;
 
             for (int j = 0; j < m.coluna; j++) {
                int soma = 0;
                Celula a = linhaA;
                Celula b = m.inicio;
 
                for (int k = 0; k < this.coluna; k++) {
                   // anda b até a coluna j
                   Celula temp = b;
                   for (int c = 0; c < j; c++) temp = temp.dir;
 
                   soma += a.elemento * temp.elemento;
                   a = a.dir;
                   b = b.inf;
                }
 
                colunaResp.elemento = soma;
                colunaResp = colunaResp.dir;
             }
 
             linhaA = linhaA.inf;
             linhaResp = linhaResp.inf;
          }
       }
 
       return resp;
    }
 
    public boolean isQuadrada() {
       return (this.linha == this.coluna);
    }
 
    public void mostrarDiagonalPrincipal() {
       if (isQuadrada()) {
          Celula atual = inicio;
          while (atual != null) {
             System.out.print(atual.elemento + " ");
             if (atual.dir != null && atual.inf != null)
                atual = atual.dir.inf;
             else
                break;
          }
          System.out.println();
       }
    }
 
    public void mostrarDiagonalSecundaria() {
       if (isQuadrada()) {
          Celula atual = inicio;
          // ir para o canto superior direito
          while (atual.dir != null)
             atual = atual.dir;
 
          while (atual != null) {
             System.out.print(atual.elemento + " ");
             if (atual.esq != null && atual.inf != null)
                atual = atual.esq.inf;
             else
                break;
          }
          System.out.println();
       }
    }
 }
 