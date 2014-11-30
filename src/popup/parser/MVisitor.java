package popup.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MVisitor extends ASTVisitor {

	List<MethodDeclaration> m = new ArrayList<MethodDeclaration>();
	
	public boolean visit(MethodDeclaration node){
		m.add(node);
		return super.visit(node);
	}
	
	public List<MethodDeclaration> getMethods() {
		return m;
	}
	
}
