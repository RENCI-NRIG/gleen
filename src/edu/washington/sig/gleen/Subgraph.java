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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropFuncArgType;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.util.iterator.ExtendedIterator;

import edu.washington.sig.gleen.javacc.grammar.ParseException;
import edu.washington.sig.gleen.javacc.grammar.PathExpression;
import edu.washington.sig.gleen.javacc.grammar.PathExpressionConstants;
import edu.washington.sig.gleen.javacc.grammar.SimpleNode;
import edu.washington.sig.gleen.path.Path;
import edu.washington.sig.gleen.util.ContextUtil;
import edu.washington.sig.gleen.util.PathUtils;

/**
 * @author Todd Detwiler
 * @date Apr 1, 2008
 *
 */
public class Subgraph extends PropertyFunctionBase
{
	Log log = LogFactory.getLog(this.getClass());
	Graph g;
	PrefixMapping queryPrefMap = null;
	
	public Subgraph()
	{
		super(PropFuncArgType.PF_ARG_LIST, PropFuncArgType.PF_ARG_LIST);
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jena.sparql.pfunction.PropertyFunctionBase#build(org.apache.jena.sparql.pfunction.PropFuncArg, org.apache.jena.graph.Node, org.apache.jena.sparql.pfunction.PropFuncArg, org.apache.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public void build(PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt)
	{
		// these checks must be passed before exec
		if(!argSubject.isList()||argSubject.getArgList().size()!=3)
			throw new QueryBuildException(this.getClass()+" subject must be list of three elements");
		if(!argObject.isList()||argObject.getArgList().size()!=3)
			throw new QueryBuildException(this.getClass()+" object must be list of three elements");
		
		// check that the output subject, predicate, and object are all variable
		if(!(argObject.getArg(0).isVariable()&& argObject.getArg(1).isVariable()&& 
				argObject.getArg(2).isVariable()))
			throw new QueryBuildException(this.getClass()+" output subject predicate object must all be variable");

		// check that input subject and object are variable 
		//if(!(argSubject.getArg(0).isVariable() && argSubject.getArg(2).isVariable()))
		//	throw new QueryBuildException(this.getClass()+" input subject and object must be variables");
		
		// check that input pathExpression is a literal
		if(!argSubject.getArg(1).isLiteral())
			throw new QueryBuildException(this.getClass()+" input path expression must be a literal");
		
		
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
	 * @see org.apache.jena.sparql.pfunction.PropertyFunctionBase#exec(org.apache.jena.sparql.engine.binding.Binding, org.apache.jena.sparql.pfunction.PropFuncArg, org.apache.jena.graph.Node, org.apache.jena.sparql.pfunction.PropFuncArg, org.apache.jena.sparql.engine.ExecutionContext)
	 */
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate,
			PropFuncArg argObject, ExecutionContext execCxt)
	{
		// get graph
		g = execCxt.getActiveGraph();
		
		// evaluate the subject and object given the binding
		PropFuncArg evalArgSubject = Substitute.substitute(argSubject, binding);
		//PropFuncArg evalArgObject = argObject.evalIfExists(binding);
		
		// get output variables
		Var subVar = (Var)argObject.getArg(0);
		Var predVar = (Var)argObject.getArg(1);
		Var objVar = (Var)argObject.getArg(2);
		
		// ensure all output vars are unbound
		if(binding.contains(subVar))
		{
			log.warn(this.getClass()+" output subject must be an unbound variable!");
			return IterLib.noResults(execCxt);
		}
		if(binding.contains(predVar))
		{
			log.warn(this.getClass()+" output predicate must be an unbound variable!");
			return IterLib.noResults(execCxt);
		}
		if(binding.contains(objVar))
		{
			log.warn(this.getClass()+" output object must be an unbound variable!");
			return IterLib.noResults(execCxt);
		}
				
		// get input subject and object after bindings have been evaluated
		Node sub = evalArgSubject.getArg(0);
		Node obj = evalArgSubject.getArg(2);
		
		// may take this out ...
		if(sub.isLiteral()||obj.isLiteral())
		{
			log.warn(this.getClass()+" neither subject or object can be a literal!");
			return IterLib.noResults(execCxt);
		}
			
		// get path expression
		Node pathExprNode = evalArgSubject.getArg(1);
		
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
		
		if(!sub.isVariable()&&obj.isVariable())
		{
			Set<Node> subjects = new HashSet<Node>();
			subjects.add(sub);
			Set<Path> paths = processParseTree(subjects,firstOp);
			Set<Triple> triples = new HashSet<Triple>();
			for(Path path : paths)
			{
				triples.addAll(path);
			}
			return genSubgraphProperties(binding, subVar, predVar, objVar, triples, execCxt);
		}
		else if(sub.isVariable()&&!obj.isVariable())
		{
			Set<Node> objects = new HashSet<Node>();
			objects.add(obj);
			Set<Path> paths = processParseTreeInv(objects,firstOp);
			Set<Triple> triples = new HashSet<Triple>();
			for(Path path : paths)
			{
				triples.addAll(path);
			}
			return genSubgraphProperties(binding, subVar, predVar, objVar, triples, execCxt);
		}
		else if(!sub.isVariable()&&!obj.isVariable())
		{
			Set<Node> subjects = new HashSet<Node>();
			subjects.add(sub);
			Set<Path> paths = processParseTree(subjects,firstOp);
			Set<Triple> triples = new HashSet<Triple>();
			for(Path path : paths)
			{
				// TODO figure out why path is sometimes empty!
				if(!path.isEmpty()&&path.getPathTail().equals(obj))
					triples.addAll(path);
			}
			return genSubgraphProperties(binding, subVar, predVar, objVar, triples, execCxt);
			}
		else
		{
			log.warn(this.getClass()+" subject and object cannot both be unbound variables!");
			return new QueryIterNullIterator(execCxt);
		}
		
	}
	
	/**
	 * Create a QueryIterator for all new bindings representing the result subgraph
	 * @param binding previous bindings
	 * @param subVar the subject variable
	 * @param predVar the predicate variable
	 * @param objVar the object variable
	 * @param triples set of triples in the result subgraph
	 * @param execCxt the current execution context
	 * @return QueryIterator for new triple bindings
	 */
	private QueryIterator genSubgraphProperties(Binding binding, Var subVar, Var predVar, 
			Var objVar, Set<Triple> triples, ExecutionContext execCxt)
	{
		// Iterate over triples and generate new bindings
		List<Binding> bindings = new ArrayList<Binding>();
		
		for (Triple currTriple : triples)
		{
			Node subject = currTriple.getSubject();
			Node property = currTriple.getPredicate();
			Node object = currTriple.getObject();
			
			BindingMap tripleBinding = new BindingHashMap(binding);
			tripleBinding.add(subVar, subject);
			tripleBinding.add(predVar, property);
			tripleBinding.add(objVar, object);
			
			bindings.add(tripleBinding);
		}
		return new QueryIterPlainWrapper(bindings.iterator(), execCxt);
	}
	
	/**
	 * Process a parse tree or subtree by calling the appropriate method depending on the root
	 * @param subjectNodes current set subject resources
	 * @param root the root of the current parse (sub)tree
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processParseTree(Set<Node> subjectNodes, SimpleNode root)
	{
		switch (root.getOperatorType())
		{
			case PathExpressionConstants.OPT:
				return processOpt(subjectNodes,root);
			case PathExpressionConstants.STAR:
				return processKleeneStar(subjectNodes, root);
			case PathExpressionConstants.PLUS:
				return processKleenePlus(subjectNodes, root);
			case PathExpressionConstants.ALT:
				return processAlt(subjectNodes, root);
			case PathExpressionConstants.CONCAT:
				return processConcat(subjectNodes, root);
			case PathExpressionConstants.PROPERTY:
				return processProperty(subjectNodes, root);
			default:
				System.err.println("invalid operator type = "+root.getOperatorType());
				break;
		}		
		
		return null;
	}
	
	/**
	 * Like processParseTree, but now the objects are defined rather than the subjects
	 * @param objectNodes current set of  object resources
	 * @param root the root of the current parse (sub)tree
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processParseTreeInv(Set<Node> objectNodes, SimpleNode root)
	{	
		switch (root.getOperatorType())
		{
			case PathExpressionConstants.OPT:
				return processOptInv(objectNodes,root);
			case PathExpressionConstants.STAR:
				return processKleeneStarInv(objectNodes, root);
			case PathExpressionConstants.PLUS:
				return processKleenePlusInv(objectNodes, root);
			case PathExpressionConstants.ALT:
				return processAltInv(objectNodes, root);
			case PathExpressionConstants.CONCAT:
				return processConcatInv(objectNodes, root);
			case PathExpressionConstants.PROPERTY:
				return processPropertyInv(objectNodes, root);
			default:
				System.err.println("invalid operator type = "+root.getOperatorType());
				break;
		}
		
		return null;
	}
	
	/**
	 * process a concatenation of path elements
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this concatenation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processConcat(Set<Node> subjectNodes, SimpleNode node)
	{
		Set<Path> resultPaths = new HashSet<Path>();
		Path emptyPath = new Path();
		emptyPath.setMatchesAny(true);
		resultPaths.add(emptyPath);
		
		// iterate over concatenated sub-expressions
		// feed results of each as subjects of next
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			Set<Path> currLevelPaths = processParseTree(subjectNodes,(SimpleNode)node.jjtGetChild(i));
			resultPaths = PathUtils.joinPaths(resultPaths, 
					currLevelPaths); 
			
			// reset subject nodes (this could probably be handled more efficiently)
			subjectNodes.clear();
			for(Path currPath : resultPaths)
			{
				subjectNodes.add(currPath.getPathTail());
			}
		}
		
		return resultPaths;
	}
	
	/**
	 * like processConcat, but now objects are defined rather than subjects
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this concatenation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processConcatInv(Set<Node> objectNodes, SimpleNode node)
	{
		Set<Path> resultPaths = new HashSet<Path>();
		Path emptyPath = new Path();
		emptyPath.setMatchesAny(true);
		resultPaths.add(emptyPath);
		
		// iterate over concatenated sub-expressions (in reverse order)
		// feed results of each as objects of next
		for (int i = node.jjtGetNumChildren()-1; i >= 0; i--)
		{
			Set<Path> currLevelPaths = processParseTreeInv(objectNodes,(SimpleNode)node.jjtGetChild(i));
			resultPaths = PathUtils.joinPaths(currLevelPaths,resultPaths); 
			
			// reset subject nodes (this could probably be handled more efficiently)
			objectNodes.clear();
			for(Path currPath : resultPaths)
			{
				objectNodes.add(currPath.getPathHead());
			}
		}
		
		return resultPaths;
	}
	
	/**
	 * process optional operator (zero or one)
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this opt
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processOpt(Set<Node> subjectNodes, SimpleNode node)
	{
		Set<Path> matchingPaths = new HashSet<Path>();
		
		// include special path to handle case of optional predicate not present
		Path flagPath = new Path();
		flagPath.setMatchesAny(true);
		matchingPaths.add(flagPath);
				
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		matchingPaths.addAll(processParseTree(subjectNodes, child));
		return matchingPaths;
	}
	
	/**
	 * like processOpt, but objects defined rather than subject
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this opt
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processOptInv(Set<Node> objectNodes, SimpleNode node)
	{
		Set<Path> matchingPaths = new HashSet<Path>();
		
		// include special path to handle case of optional predicate not present
		Path flagPath = new Path();
		flagPath.setMatchesAny(true);
		matchingPaths.add(flagPath);
		
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		matchingPaths.addAll(processParseTreeInv(objectNodes,child));
		return matchingPaths;
	}
	
	/**
	 * process Kleene star operator (zero or more)
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processKleeneStar(Set<Node> subjectNodes, SimpleNode node)
	{
		Set<Path> matchingPaths = new HashSet<Path>();

		// include special path to handle case of optional predicate not present
		Path flagPath = new Path();
		flagPath.setMatchesAny(true);
		
		matchingPaths.add(flagPath);
		matchingPaths.addAll(processKleenePlus(subjectNodes,node));
		return matchingPaths;
	}
	
	/**
	 * like processKleeneStar, but objects are defined rather than subjects
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processKleeneStarInv(Set<Node>  objectNodes, SimpleNode node)
	{	
		Set<Path> matchingPaths = new HashSet<Path>();

		// include special path to handle case of optional predicate not present
		Path flagPath = new Path();
		flagPath.setMatchesAny(true);
		
		matchingPaths.add(flagPath);
		matchingPaths.addAll(processKleenePlusInv(objectNodes,node));
		return matchingPaths;
	}
	
	/**
	 * process Kleene plus operator (one or more)
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processKleenePlus(Set<Node> subjectNodes, SimpleNode node)
	{
		Set<Path> matchingPaths = new HashSet<Path>();
		boolean firstPass = true;
		
		Set<Path> resultPaths = new HashSet<Path>();
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		
		Set<Node> nodesSeen = new HashSet<Node>(subjectNodes);
		
		while(!subjectNodes.isEmpty())
		{
			Set<Path> currLevelPaths = processParseTree(subjectNodes,child);
			if(firstPass)
			{
				resultPaths = currLevelPaths;
				firstPass = false;
			}
			else
				resultPaths = PathUtils.joinPaths(resultPaths, currLevelPaths); 
			
			// add to matchingPaths
			matchingPaths.addAll(resultPaths);
			
			// reset subject nodes (this could probably be handled more efficiently)
			subjectNodes.clear();
			for(Path currPath : resultPaths)
			{
				Node currPathTail = currPath.getPathTail();
				boolean isNew = nodesSeen.add(currPathTail);
				if(isNew)
					subjectNodes.add(currPathTail);
			}
		}
				
		return matchingPaths;
	}
	
	/**
	 * like processKleenePlus, but objects are defined rather than subjects
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processKleenePlusInv(Set<Node> objectNodes, SimpleNode node)
	{
		Set<Path> matchingPaths = new HashSet<Path>();
		boolean firstPass = true;
		
		Set<Path> resultPaths = new HashSet<Path>();
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		
		Set<Node> nodesSeen = new HashSet<Node>(objectNodes);
		
		while(!objectNodes.isEmpty())
		{
			Set<Path> currLevelPaths = processParseTreeInv(objectNodes,child);
			if(firstPass)
			{
				resultPaths = currLevelPaths;
				firstPass = false;
			}
			else
				resultPaths = PathUtils.joinPaths(currLevelPaths, resultPaths); 
			
			// add to matchingPaths
			matchingPaths.addAll(resultPaths);
			
			// reset subject nodes (this could probably be handled more efficiently)
			objectNodes.clear();
			for(Path currPath : resultPaths)
			{
				Node currPathHead = currPath.getPathHead();
				boolean isNew = nodesSeen.add(currPathHead);
				if(isNew)
					objectNodes.add(currPathHead);
			}
		}
		
		return matchingPaths;
	}
	
	/**
	 * process single relationship link (single property)
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processProperty(Set<Node> subjectNodes, SimpleNode node)
	{
		Node propertyNode = getProperty(node);
		Set<Path> matchingPaths = new HashSet<Path>();
		
		for(Node subject : subjectNodes)
		{
			ExtendedIterator ei = g.find(subject, propertyNode, Node.ANY);
			while(ei.hasNext())
			{
				Triple currTriple = (Triple)ei.next();
				Path newPath = new Path();
				newPath.add(currTriple);
				matchingPaths.add(newPath);
			}
		}
		return matchingPaths;
	}
	
	/**
	 * like processRelNode, but objects are defined rather than subjects
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processPropertyInv(Set<Node> objectNodes, SimpleNode node)
	{
		Node propertyNode = getProperty(node);
		Set<Path> matchingPaths = new HashSet<Path>();
		
		for(Node object : objectNodes)
		{
			ExtendedIterator ei = g.find(Node.ANY, propertyNode, object);
			while(ei.hasNext())
			{
				Triple currTriple = (Triple)ei.next();
				Path newPath = new Path();
				newPath.add(currTriple);
				matchingPaths.add(newPath);
			}
		}
		return matchingPaths;
	}
	
	/**
	 * Process alternation operator (logical OR)
	 * This currently assumes alternation over simple rels
	 * @param subjectNodes the current set of subject resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processAlt(Set<Node> subjectNodes, SimpleNode node)
	{
		//Set<Node> propertyNodes = getAltProperties(node);
		Set<Path> matchingPaths = new HashSet<Path>();
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);

			matchingPaths.addAll(processParseTree(subjectNodes, child));
		}
		/*
		for(Node subject : subjectNodes)
		{
			for(Node propertyNode : propertyNodes)
			{
				ExtendedIterator ei = g.find(subject, propertyNode, Node.ANY);
				while(ei.hasNext())
				{
					Triple currTriple = (Triple)ei.next();
					Path newPath = new Path();
					newPath.add(currTriple);
					matchingPaths.add(newPath);
				}
			}
		}
		*/
		return matchingPaths;
	}
	
	/**
	 * like processAlt, but objects are defined rather than subjects
	 * @param objectNodes the current set of object resources
	 * @param node the root of the parse (sub)tree for this operation
	 * @return set of graph paths consistent with the path pattern 
	 * represented by the given parse (sub)tree
	 */
	private Set<Path> processAltInv(Set<Node> objectNodes, SimpleNode node)
	{
		//Set<Node> propertyNodes = getAltProperties(node);
		Set<Path> matchingPaths = new HashSet<Path>();
		
		for (int i = 0; i < node.jjtGetNumChildren(); i++)
		{
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);

			matchingPaths.addAll(processParseTreeInv(objectNodes, child));
		}
		/*
		for(Node object : objectNodes)
		{
			for(Node propertyNode : propertyNodes)
			{
				ExtendedIterator ei = g.find(Node.ANY, propertyNode, object);
				while(ei.hasNext())
				{
					Triple currTriple = (Triple)ei.next();
					Path newPath = new Path();
					newPath.add(currTriple);
					matchingPaths.add(newPath);
				}
			}
		}
		*/
		return matchingPaths;
	}
	
	/**
	 * get the property node for a given property string
	 * @param node the root of a parse tree containing a property string
	 * @return the property node for the given string
	 */
	private Node getProperty(SimpleNode node)
	{
		String propertyText = node.getOperator();
		
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
		
		String expandedPropName = prefixMapping.expandPrefix(propertyText);

		Node propertyNode = NodeFactory.createURI(expandedPropName);
		
		return propertyNode;
	}
	
	/**
	 * get the set of property nodes for a given set of string
	 * (separated by alternation operators)
	 * @param node the root of a parse tree containing an alternation of property strings
	 * @return set of property nodes for input strings
	 */
	private Set<Node> getAltProperties(SimpleNode node)
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
			
			String propertyText = child.getOperator();
						
			String expandedPropName = prefixMapping.expandPrefix(propertyText);

			Node propertyNode = NodeFactory.createURI(expandedPropName);
			altNodeList.add(propertyNode);
		}
		
		return altNodeList;
	}
}
