/* Generated By:JJTree: Do not edit this line. ASTUnaryOpExpr.java */

package edu.washington.sig.gleen.javacc.grammar;

public @SuppressWarnings("all")
class ASTUnaryOpExpr extends SimpleNode
{
	//private String operator = null;
	//private int operatorType;
	
	public ASTUnaryOpExpr(int id)
	{
		super(id);
	}

	public ASTUnaryOpExpr(PathExpression p, int id)
	{
		super(p, id);
	}

	public String toString()
	{
		return "uniOp: "+this.getOperator()+" (type="+this.getOperatorType()+")";
	}
}
