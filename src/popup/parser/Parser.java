package popup.parser;

import org.eclipse.core.commands.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import popup.popup.actions.NewAction;


public class Parser extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IProject proj = NewAction.getproject();
		try{
			if(proj.isNatureEnabled("org.eclipse.jdt.core.javanature")){
				
				IPackageFragment[] packages = JavaCore.create(proj).getPackageFragments();
				for(IPackageFragment p:packages){
					if(p.getKind() == IPackageFragmentRoot.K_SOURCE){
						
						for(ICompilationUnit u: p.getCompilationUnits()){
							
							CompilationUnit parse = parse(u);
							MVisitor v =  new MVisitor();
							parse.accept(v);
							
							for(MethodDeclaration method : v.getMethods()){
								System.out.println("Method name: " + method.getName() + "\tReturn type: " + method.getReturnType2());
							}
						}
					}
					
				}

				
			}
			
		}catch(CoreException e){
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	
	
	private static CompilationUnit parse(ICompilationUnit unit){
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}
