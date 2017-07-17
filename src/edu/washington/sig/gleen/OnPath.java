/**
 * Copyright 2008 University of Washington Structural Informatics Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.washington.sig.gleen;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryBuildException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterExtendByVar;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArgType;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.washington.sig.gleen.javacc.grammar.ParseException;
import edu.washington.sig.gleen.javacc.grammar.PathExpression;
import edu.washington.sig.gleen.javacc.grammar.PathExpressionConstants;
import edu.washington.sig.gleen.javacc.grammar.SimpleNode;
import edu.washington.sig.gleen.javacc.grammar.TokenMgrError;
import edu.washington.sig.gleen.util.ContextUtil;

/**
 * @author Todd Detwiler
 * @date Apr 1, 2008
 *
 */
public class OnPath extends PropertyFunctionBase
{
	private Log log = LogFactory.getLog(this.getClass());
	private Graph g;
	private PrefixMapping queryPrefMap = null;
	
	public OnPath()
	{
		super(PropFuncArgType.PF_ARG_SINGLE, PropFuncArgType.PF_ARG_LIST);
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase#build(com.hp.hpl.jena.sparql.pfunction.PropFuncArg, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.sparql.pfunction.PropFuncArg, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public void build(PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt)
	{
		// these checks must be passed before exec
		if(argSubject.isList())
			throw new QueryBuildException(this.getClass()+" subject must be a single node or variable, not a list");
		if(!argObject.isList()||argObject.getArgList().size()!=2)
			throw new QueryBuildException(this.getClass()+" object must be list of two elements");
		
		// get query prefix map
		Context cxt = execCxt.getContext();
		if(cxt.isDefined(ARQConstants.sysCurrentQuery))
		{
			Query query = (Query)cxt.get(ARQConstants.sysCurrentQuery);
			queryPrefMap = query.getPrefixMapping();
		}
		else if(cxt.isDefined(ContextUtil.getQueryPrefMapSymbol()))
		{
			queryPrefMap = (PrefixMapping)cxt.get(ContextUtil.getQueryPrefMapSymbol());
		}

		if(queryPrefMap==null)
			throw new QueryBuildException(this.getClass()+" query prefix mapping is null");
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.sparql.pfunction.PropertyFunctionBase#exec(com.hp.hpl.jena.sparql.engine.binding.Binding, com.hp.hpl.jena.sparql.pfunction.PropFuncArg, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.sparql.pfunction.PropFuncArg, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt)
	{
		// get graph
		g = execCxt.getActiveGraph();
		
		// evaluate the subject and object given the binding
		PropFuncArg evalArgSubject = Substitute.substitute(argSubject, binding);
		PropFuncArg evalArgObject = Substitute.substitute(argObject, binding);
			
		// make sure that first node in object list (the path expression) is a literal
		Node pathExprNode = evalArgObject.getArg(0);
		if(!pathExprNode.isLiteral())
		{
			log.warn(this.getClass()+" path string must be a literal");
			return new QueryIterNullIterator(execCxt);
		}
		
		// generate parse tree for path expression
		String pathExprString = pathExprNode.getLiteralLexicalForm();
		PathExpression pathExpr = new PathExpression(new StringReader(pathExprString));
		SimpleNode rootNode;
		try
		{
			rootNode = pathExpr.Start();
		}
		catch (ParseException e)
		{
			log.warn(this.getClass()+" parseException! "+e.getMessage());
			return IterLib.noResults(execCxt);
		}
		
		// start node can have only one child
		if(rootNode.jjtGetNumChildren()!=1)
		{
			log.warn(this.getClass()+" improperly formed AST!");
			return IterLib.noResults(execCxt);
		}
		SimpleNode firstOp = (SimpleNode)rootNode.jjtGetChild(0);
		
		// get subject and object after bindings have been evaluated
		Node sub = evalArgSubject.getArg();
		Node obj = evalArgObject.getArg(1);
		if(sub.isLiteral()||obj.isLiteral())
		{
			log.warn(this.getClass()+" neither subject or object can be a literal!");
			return IterLib.noResults(execCxt);
		}
	
		if(!sub.isVariable()&&obj.isVariable())
		{
			Var objVar = (Var)obj;
			Set<Node> subjects = new HashSet<Node>();
			subjects.add(sub);
			Set<Node> reachableNodes = processParseTree(subjects,firstOp);
			return new QueryIterExtendByVar(binding,objVar,reachableNodes.iterator(),execCxt);
		}
		else if(sub.isVariable()&&!obj.isVariable())
		{
			Var subVar = (Var)sub;
			Set<Node> objects = new HashSet<Node>();
			objects.add(obj);
			Set<Node> reachableNodes = processParseTreeInv(objects,firstOp);
			return new QueryIterExtendByVar(binding,subVar,reachableNodes.iterator(),execCxt);
		}
		else if(!sub.isVariable()&&!obj.isVariable())
		{
			/*
			 * NOTE: implementation here is inefficient (no early exit if result is found).
			 */
			
			// either sub or obj or both must be a bound variable
			if(argSubject.getArg().isVariable())
			{
				Set<Node> objects = new HashSet<Node>();
				objects.add(obj);
				
				Set<Node> reachableNodes = processParseTreeInv(objects,firstOp);
				if(reachableNodes.contains(sub))
				{
					Set<Node> subjects = new HashSet<Node>();
					subjects.add(sub);
					return new QueryIterExtendByVar(binding,(Var)argSubject.getArg(),
							subjects.iterator(),execCxt);
				}
				else
				{
					return new QueryIterNullIterator(execCxt);
				}
			}
			else if(argObject.getArg(1).isVariable())
			{
				Set<Node> subjects = new HashSet<Node>();
				subjects.add(sub);
				
				Set<Node> reachableNodes = processParseTree(subjects,firstOp);
				if(reachableNodes.contains(obj))
				{
					Set<Node> objects = new HashSet<Node>();
					objects.add(obj);
					return new QueryIterExtendByVar(binding,(Var)argObject.getArg(1),
							objects.iterator(),execCxt);
				}
				else
				{
					return new QueryIterNullIterator(execCxt);
				}
			}
			else
			{
				log.warn(this.getClass()+" either subject or object must be a variable!");
				return IterLib.noResults(execCxt);
			}
		}
		else
		{
			log.warn(this.getClass()+" subject and object cannot both be unbound variables!");
			return new QueryIterNullIterator(execCxt);
		}
	}
	
	/**
	 * Process a parse tree or subtree by calling the appropriate method depending on the root
	 * @param subjects current set of subject resources
	 * @param root the root of the current parse (sub)tree
	 * @return set of resources reachable, from subjects, via paths matching the pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Node> processParseTree(Set<Node> subjects, SimpleNode root)
	{
		switch (root.getOperatorType())
		{
			case PathExpressionConstants.OPT:
				return processOpt(subjects,root);
			case PathExpressionConstants.STAR:
				return processKleeneStar(subjects, root);
			case PathExpressionConstants.PLUS:
				return processKleenePlus(subjects, root);
			case PathExpressionConstants.ALT:
				return processAlt(subjects, root);
			case PathExpressionConstants.CONCAT:
				return processConcat(subjects, root);
			case PathExpressionConstants.PROPERTY:
				return processRelNode(subjects, root);
			default:
				System.err.println("invalid operator type = "+root.getOperatorType());
				break;
		}
		
		return null;
	}
	
	/**
	 * Like processParseTree, but now the object is defined rather than the subject
	 * @param objects current set of object resources
	 * @param root root of the current parse (sub)tree
	 * @return set of resources from which given objects can be reached via paths 
	 * matching the pattern represented by this parse (sub)tree
	 */
	private Set<Node> processParseTreeInv(Set<Node> objects, SimpleNode root)
	{	
		switch (root.getOperatorType())
		{
			case PathExpressionConstants.OPT:
				return processOptInv(objects,root);
			case PathExpressionConstants.STAR:
				return processKleeneStarInv(objects, root);
			case PathExpressionConstants.PLUS:
				return processKleenePlusInv(objects, root);
			case PathExpressionConstants.ALT:
				return processAltInv(objects, root);
			case PathExpressionConstants.CONCAT:
				return processConcatInv(objects, root);
			case PathExpressionConstants.PROPERTY:
				return processRelNodeInv(objects, root);
			default:
				System.err.println("invalid operator type = "+root.getOperatorType());
				break;
		}
			
		return null;
	}
	
	/**
	 * process a concatenation of path elements
	 * @param subjectSet the current set of subject resources
	 * @param node the root of the parse (sub)tree for this concatenation
	 * @return set of resources reachable via this concatenation of path elements
	 */
	private Set<Node> processConcat(Set<Node> subjectSet, SimpleNode node)
	{
		Set<Node> results = new HashSet<Node>(subjectSet);
		
		// iterate over concatenated sub-expressions
		// feed results of each as subjects of next
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			results = processParseTree(results,(SimpleNode)node.jjtGetChild(i));
		}
		
		return results;
	}
	
	/**
	 * like processConcat, but now objects are defined rather than subjects
	 * @param objectSet the current set of object resources
	 * @param node the root of the parse (sub)tree for this concatenation
	 * @return set of resources from which a resource in objectSet can be reached 
	 * via this concatenation of path elements
	 */
	private Set<Node> processConcatInv(Set<Node> objectSet, SimpleNode node)
	{
		Set<Node> results = new HashSet<Node>(objectSet);
		
		// iterate over concatenated sub-expressions (in reverse order)
		// feed results of each as objects of next
		for (int i = node.jjtGetNumChildren()-1; i >= 0; i--)
		{
			results = processParseTreeInv(results,(SimpleNode)node.jjtGetChild(i));
		}
		
		return results;
	}
	
	/**
	 * process optional operator (zero or one)
	 * @param subjects the current set of subject resources
	 * @param node the root of the parse (sub)tree for this opt
	 * @return set of resources reachable via the path pattern represented
	 * by this parse tree
	 */
	private Set<Node> processOpt(Set<Node> subjects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		reachableNodes.addAll(subjects);
		
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		reachableNodes.addAll(processParseTree(subjects, child));
		return reachableNodes;
	}
	
	/**
	 * like processOpt, but objects defined rather than subject
	 * @param objects the current set of object resources
	 * @param node the root of the parse (sub)tree for this opt
	 * @return set of resources from which a resource in objects can be reached 
	 * via the path pattern represented by this parse tree
	 */
	private Set<Node> processOptInv(Set<Node> objects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		reachableNodes.addAll(objects);
		
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		reachableNodes.addAll(processParseTreeInv(objects,child));
		return reachableNodes;
	}
	
	/**
	 * process Kleene star operator (zero or more)
	 * @param subjects the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources reachable via the path pattern represented
	 * by this parse tree
	 */
	private Set<Node> processKleeneStar(Set<Node> subjects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		reachableNodes.addAll(subjects);
		reachableNodes.addAll(processKleenePlus(subjects,node));
		return reachableNodes;
	}
	
	/**
	 * like processKleeneStar, but objects are defined rather than subjects
	 * @param objects the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources from which a resource in objects can be reached 
	 * via the path pattern represented by this parse tree
	 */
	private Set<Node> processKleeneStarInv(Set<Node> objects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		reachableNodes.addAll(objects);
		reachableNodes.addAll(processKleenePlusInv(objects,node));
		return reachableNodes;
	}
	
	/**
	 * process Kleene plus operator (one or more)
	 * @param subjects the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources reachable via the path pattern represented
	 * by this parse tree
	 */
	private Set<Node> processKleenePlus(Set<Node> subjects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);

		Set<Node> currLevelResults = new HashSet<Node>(subjects);
		while(!currLevelResults.isEmpty())
		{
			currLevelResults = processParseTree(currLevelResults, child);
			
			// test to deal with cycles, may be inefficient
			Iterator<Node> resultsIt = currLevelResults.iterator();
			while(resultsIt.hasNext())
			{
				Node currResult = resultsIt.next();
				if(!reachableNodes.add(currResult))
				{
					resultsIt.remove();
				}
			}
				
			
			reachableNodes.addAll(currLevelResults);
		} 
		
		return reachableNodes;
	}
	
	/**
	 * like processKleenePlus, but objects are defined rather than subjects
	 * @param objects the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources from which a resource in objects can be reached 
	 * via the path pattern represented by this parse tree
	 */
	private Set<Node> processKleenePlusInv(Set<Node> objects, SimpleNode node)
	{
		Set<Node> reachableNodes = new HashSet<Node>();
		
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);

		Set<Node> currLevelResults = new HashSet<Node>(objects);
		while(!currLevelResults.isEmpty())
		{
			currLevelResults = processParseTreeInv(currLevelResults, child);
			
			// test to deal with cycles, may be inefficient
			Iterator<Node> resultsIt = currLevelResults.iterator();
			while(resultsIt.hasNext())
			{
				Node currResult = resultsIt.next();
				if(!reachableNodes.add(currResult))
				{
					resultsIt.remove();
				}
			}
				
			
			reachableNodes.addAll(currLevelResults);
		} 
		
		return reachableNodes;
	}
	
	/**
	 * process single relationship link (single property)
	 * @param subjects the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources reachable via the path pattern represented
	 * by this parse tree
	 */
	private Set<Node> processRelNode(Set<Node> subjects, SimpleNode node)
	{
		Node relNode = getRelNode(node);
		Set<Node> reachableNodes = new HashSet<Node>();
		
		for(Node subject : subjects)
		{
			ExtendedIterator ei = g.find(subject, relNode, Node.ANY);
			while(ei.hasNext())
			{
				Triple currTriple = (Triple)ei.next();
				Node currNode = currTriple.getObject();
				reachableNodes.add(currNode);
			}
		}
		return reachableNodes;
	}
	
	/**
	 * like processRelNode, but objects are defined rather than subjects
	 * @param objects the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources from which a resource in objects can be reached 
	 * via the path pattern represented by this parse tree
	 */
	private Set<Node> processRelNodeInv(Set<Node> objects, SimpleNode node)
	{
		Node relNode = getRelNode(node);
		Set<Node> reachableNodes = new HashSet<Node>();
		
		for(Node object : objects)
		{
			ExtendedIterator ei = g.find(Node.ANY, relNode, object);
			while(ei.hasNext())
			{
				Triple currTriple = (Triple)ei.next();
				Node currNode = currTriple.getSubject();
				reachableNodes.add(currNode);
			}
		}
		return reachableNodes;
	}
	
	/**
	 * Process alternation operator (logical OR)
	 * @param subjects the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources reachable via the path pattern represented
	 * by this parse tree
	 */
	private Set<Node> processAlt(Set<Node> subjects, SimpleNode node)
	{
		//Set<Node> relNodes = getAltRelNodes(node);
		Set<Node> reachableNodes = new HashSet<Node>();
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);

			reachableNodes.addAll(processParseTree(subjects, child));
		}
		/*
		for(Node subject : subjects)
		{
			for(Node relNode : relNodes)
			{
				ExtendedIterator ei = g.find(subject, relNode, Node.ANY);
				while(ei.hasNext())
				{
					Triple currTriple = (Triple)ei.next();
					Node currNode = currTriple.getObject();
					reachableNodes.add(currNode);
				}
			}
		}
		*/
		return reachableNodes;
	}
	
	/**
	 * like processAlt, but objects are defined rather than subjects
	 * @param objects the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of resources from which a resource in objects can be reached 
	 * via the path pattern represented by this parse tree
	 */
	private Set<Node> processAltInv(Set<Node> objects, SimpleNode node)
	{
		//Set<Node> relNodes = getAltRelNodes(node);
		Set<Node> reachableNodes = new HashSet<Node>();
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);

			reachableNodes.addAll(processParseTreeInv(objects, child));
		}
		/*
		for(Node object : objects)
		{
			for(Node relNode : relNodes)
			{
				ExtendedIterator ei = g.find(Node.ANY, relNode, object);
				while(ei.hasNext())
				{
					Triple currTriple = (Triple)ei.next();
					Node currNode = currTriple.getSubject();
					reachableNodes.add(currNode);
				}
			}
		}
		*/
		return reachableNodes;
	}
	
	/**
	 * get the property node for a given property string
	 * @param node the root of a parse tree containing a property string
	 * @return the property node for the given string
	 */
	private Node getRelNode(SimpleNode node)
	{
		String relText = node.getOperator();
		
		// get query prefix mapping if possible, if not use graph (which may not work)
		PrefixMapping prefixMapping = null;
		if(queryPrefMap!=null)
		{
			prefixMapping = queryPrefMap;
		}
		else
		{
			prefixMapping = g.getPrefixMapping();
		}
		
		String expandedRelName = prefixMapping.expandPrefix(relText);

		Node relNode = Node.createURI(expandedRelName);
		
		return relNode;
	}
	
	/**
	 * get the set of property nodes for a given set of string
	 * (separated by alternation operators)
	 * @param node the root of a parse tree containing an alternation of property strings
	 * @return set of property nodes for input strings
	 */
	private Set<Node> getAltRelNodes(SimpleNode node)
	{
		Set<Node> altNodeList = new HashSet<Node>();
		
		// get query prefix mapping if possible, if not use graph (which may not work)
		PrefixMapping prefixMapping = null;
		if(queryPrefMap!=null)
		{
			prefixMapping = queryPrefMap;
		}
		else
		{
			prefixMapping = g.getPrefixMapping();
		}
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			
			String relText = child.getOperator();
						
			String expandedRelName = prefixMapping.expandPrefix(relText);

			Node relNode = Node.createURI(expandedRelName);
			altNodeList.add(relNode);
		}
		
		return altNodeList;
	}
	
	void test(String pathExpression)
	{
		StringReader queryReader = new StringReader(pathExpression);
		
		PathExpression parser = new PathExpression(queryReader);

		try
		{
			System.out.println("pathExpression = " + pathExpression);
			System.out.println();

			SimpleNode root = parser.Start();
			
			Queue nodeQueue = new LinkedList<SimpleNode>();
			nodeQueue.offer(root);
			SimpleNode currNode;
			
			while(( currNode = (SimpleNode)nodeQueue.poll())!=null)
			{
				for(int childCount=0; childCount<currNode.jjtGetNumChildren(); childCount++)
				{
					SimpleNode nextNode = (SimpleNode)currNode.jjtGetChild(childCount);
					nodeQueue.offer(nextNode);
					processParseTree(new HashSet<Node>(),nextNode);
				}
			}
			//root.dump("");
			
			/*
			for(int i=0; i<root.jjtGetNumChildren(); i++)
			{
				System.err.println("child class = "+root.jjtGetChild(i).getClass());
			}
			*/
		}
		catch (ParseException e)
		{
			System.out.print(e.getMessage());
			System.out.println();
		}
		catch (TokenMgrError tke)
		{
			System.out.print(tke.getMessage());
			System.out.println();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		OnPath subgraph = new OnPath();
		subgraph.test(args[0]);
	}

}
