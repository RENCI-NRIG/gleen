/* Generated By:JJTree: Do not edit this line. ASTBinaryOpExpr.java */

package edu.washington.sig.gleen.javacc.grammar;

public @SuppressWarnings("all")
class ASTBinaryOpExpr extends SimpleNode
{
	//private String operator = null;
	//private int operatorType;
	
	public ASTBinaryOpExpr(int id)
	{
		super(id);
	}

	public ASTBinaryOpExpr(PathExpression p, int id)
	{
		super(p, id);
	}

	public String toString()
	{
		return "binOp: "+this.getOperator()+" (type="+this.getOperatorType()+")";
	}
}
