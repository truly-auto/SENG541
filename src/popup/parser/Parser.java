package popup.parser;

import javax.swing.JTextArea;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;

import popup.popup.actions.NewAction;


public class Parser extends AbstractHandler {

	private JTextArea errors;

	
	public Parser(JTextArea e){
		errors = e;
	}
	
	
	public void analyseMethods(IProject p)throws JavaModelException{
		
		IPackageFragment[] packages = JavaCore.create(p).getPackageFragments();
		for(IPackageFragment pf: packages){
			if(pf.getKind()==IPackageFragmentRoot.K_SOURCE){
				createAST(pf);
			}
		}
	}
	
	
	public void createAST(IPackageFragment mypackage) throws JavaModelException {
		
		for(ICompilationUnit u: mypackage.getCompilationUnits()){
							
				CompilationUnit parse = parse(u);
				
				IProblem[] projectErrors = parse.getProblems();
				//NewAction.problems = new String [projectErrors.length];
					
				int count=0;
				
				for(IProblem e: projectErrors){
					NewAction.problems.add(e.toString());
					errors.append(NewAction.problems.get(count));
					count++;
				}
				
				MVisitor v =  new MVisitor();
				parse.accept(v);		
							
			/*	for(MethodDeclaration method : v.getMethods()){
				System.out.println("Method name: " + method.getName() + "\tReturn type: " + method.getReturnType2());
				}*/
		}
	}
	
	
	
	private static CompilationUnit parse(ICompilationUnit unit){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
