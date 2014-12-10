package popup.popup.actions;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;

public class APIRecommend {
	private String rAPIParameters; 
	private String rAPIReturns; 
	private IMethod[] oJar;
	private IMethod[] nJar;
	private ArrayList<String> problems=null;
	
	public APIRecommend(IMethod [] oldJar, IMethod [] newJar, ArrayList<String> errors){
		this.oJar = oldJar;
		this.nJar = newJar;
		
		System.out.println("The number of problems:   "+errors.size());
		
			problems = errors;
	}
	
	public ArrayList<String> getMethodParameters(){
		for(int i = 0; i < NewAction.problems.size(); i++) {
			String [] parse = NewAction.problems.get(i).split("\\s+");
			
			if (ValidError(parse[0])) 
				NewAction.problems.set(i, getErrorMethod(NewAction.problems.get(i)));
			else 
				NewAction.problems.set(i, null);
		}
		return NewAction.problems;//exception here
	}
	
	/**All this function does is it parses the string to get the method name and errors
	 * This is used in getMethodParameters, maybe change it by putting it in that function
	 * Just make it different from theirs!!!!*/
	static String getErrorMethod(String errorMessage) {
		String [] parse = errorMessage.split("\\s+");
		String temp = "";
		
		for(int i = 3; i < parse.length; i++)
			temp += parse[i] + " ";
	
		parse = temp.split("\\)");
		
		return parse[0] + ")";
	}
	
	/**change this function by possibly putting it into getMethodParameters b/c thats 
	 * where it is used. 
	 * All this does is it checks what type of error it is and returns true or false.*/
	static Boolean ValidError(String errorType) {
		if (errorType.equals("Pb(100)") || errorType.equals("Pb(103)") 
		 || errorType.equals("Pb(115)")|| errorType.equals("Pb(133)")) {
			return true;
		} else 
			return false;
	}
	
	/**The following two are the algorithms for get paramters and return type, see if you can change
	 * these two functions but be careful in moving arrays around b/c this function access the fields
	 * defined in the NewAction (main file) so see what you can do here!!!!!!!!!!*/
	public String getParameters() throws JavaModelException {
				
				for(int o_method=0; o_method < NewAction.arrayOldJar.length; o_method++)
					for(int n_method=0; n_method < NewAction.arrayNewJar.length; n_method++)
					if(NewAction.paraMethodOld[o_method].length!=NewAction.paraMethodNew[n_method].length){
						rAPIParameters+="The method: "+ NewAction.arrayOldJar[o_method]+"\n"+"Does not match!\n";
						//There are going to be many methods that don't match
					}
					else{
						for(int arg=0; arg<NewAction.paraMethodOld[o_method].length; arg++){
							if(NewAction.paraMethodOld[o_method][arg].compareTo(NewAction.paraMethodNew[n_method][arg])==0){
								rAPIParameters+="The method parameters of: \n"+ NewAction.arrayOldJar[o_method]+"\n matches with"
										+ "the new method parameters of: \n" + NewAction.arrayNewJar[n_method];
								
							}
						}
					}
					
				
			

		return rAPIParameters;
	}
	//returntypeold and returntypenew are not variables in the Migration class
	public String getReturnType() throws JavaModelException {
/*		for(int o_method=0; o_method < NewAction.arrayOldJar.length; o_method++)
			for(int n_method=0; n_method < NewAction.arrayNewJar.length; n_method++)
					if(NewAction.oldjarpath.equals(NewAction.newjarpath)){
						if(NewAction.returnTypeOld[o_method].compareTo(NewAction.returnTypeNew[n_method])==0){
							rAPIReturns+="Method return type of: \n"+ NewAction.arrayOldJar[o_method]+"\n matches with the return"
									+ "type of : \n" + NewAction.arrayNewJar[n_method];
						}
						
					}
		
			return rAPIReturns;*/
			return "Getreturntype";
		}	
		
}

