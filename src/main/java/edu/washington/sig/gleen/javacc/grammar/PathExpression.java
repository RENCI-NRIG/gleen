/* Generated By:JJTree&JavaCC: Do not edit this line. PathExpression.java */
package edu.washington.sig.gleen.javacc.grammar;

import java.io.ByteArrayInputStream;

public class PathExpression/*@bgen(jjtree)*/implements PathExpressionTreeConstants, PathExpressionConstants {/*@bgen(jjtree)*/
  protected JJTPathExpressionState jjtree = new JJTPathExpressionState();
  public static void main(String args[]) {
    ByteArrayInputStream str = new ByteArrayInputStream(args[0].getBytes());
    PathExpression t = new PathExpression(str);
    try {
      SimpleNode n = t.Start();
      n.dump("");
      System.out.println("Thank you.");
    } catch (Exception e) {
      System.out.println("Oops.");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  final public SimpleNode Start() throws ParseException {
                      /*@bgen(jjtree) Start */
  ASTStart jjtn000 = new ASTStart(JJTSTART);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Expr();
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
          {if (true) return jjtn000;}
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
    throw new Error("Missing return statement in function");
  }

  final public void Expr() throws ParseException {
    BinaryOpExpr();
  }

  final public void SubExpr() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LGROUP:
      jj_consume_token(LGROUP);
      Expr();
      jj_consume_token(RGROUP);
      break;
    case PROPERTY:
      SimpTerm();
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void BinaryOpExpr() throws ParseException {
                                              /*@bgen(jjtree) #BinaryOpExpr( t != null) */
                                               ASTBinaryOpExpr jjtn000 = new ASTBinaryOpExpr(JJTBINARYOPEXPR);
                                               boolean jjtc000 = true;
                                               jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      UnaryOpExpr();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case CONCAT:
      case ALT:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case CONCAT:
          label_1:
          while (true) {
            t = jj_consume_token(CONCAT);
            BinaryOpExpr();
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case CONCAT:
              ;
              break;
            default:
              jj_la1[1] = jj_gen;
              break label_1;
            }
          }
          break;
        case ALT:
          label_2:
          while (true) {
            t = jj_consume_token(ALT);
            BinaryOpExpr();
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case ALT:
              ;
              break;
            default:
              jj_la1[2] = jj_gen;
              break label_2;
            }
          }
          break;
        default:
          jj_la1[3] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[4] = jj_gen;
        ;
      }
          jjtree.closeNodeScope(jjtn000,  t != null);
          jjtc000 = false;
                if(t!=null)
                {
                        jjtn000.setOperatorType(t.kind);
                        jjtn000.setOperator(t.image);
                }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000,  t != null);
          }
    }
  }

  final public void UnaryOpExpr() throws ParseException {
                                            /*@bgen(jjtree) #UnaryOpExpr( t != null) */
                                             ASTUnaryOpExpr jjtn000 = new ASTUnaryOpExpr(JJTUNARYOPEXPR);
                                             boolean jjtc000 = true;
                                             jjtree.openNodeScope(jjtn000);Token t = null;
    try {
      SubExpr();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STAR:
      case PLUS:
      case OPT:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case STAR:
          t = jj_consume_token(STAR);
          break;
        case PLUS:
          t = jj_consume_token(PLUS);
          break;
        case OPT:
          t = jj_consume_token(OPT);
          break;
        default:
          jj_la1[5] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[6] = jj_gen;
        ;
      }
          jjtree.closeNodeScope(jjtn000,  t != null);
          jjtc000 = false;
                if(t!=null)
                {
                        jjtn000.setOperatorType(t.kind);
                        jjtn000.setOperator(t.image);
                }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000,  t != null);
          }
    }
  }

  final public void SimpTerm() throws ParseException {
                   /*@bgen(jjtree) SimpTerm */
                    ASTSimpTerm jjtn000 = new ASTSimpTerm(JJTSIMPTERM);
                    boolean jjtc000 = true;
                    jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(PROPERTY);
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
                jjtn000.setOperatorType(t.kind);
        jjtn000.setOperator(t.image);
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  /** Generated Token Manager. */
  public PathExpressionTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x4080,0x200,0x400,0x600,0x600,0x3800,0x3800,};
   }

  /** Constructor with InputStream. */
  public PathExpression(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public PathExpression(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new PathExpressionTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public PathExpression(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new PathExpressionTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public PathExpression(PathExpressionTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(PathExpressionTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List jj_expentries = new java.util.ArrayList();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[16];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 7; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 16; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
