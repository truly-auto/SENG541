package popup.popup.actions;


import java.awt.Dimension;
import java.awt.List;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.AbstractElementListSelectionDialog;

import popup.parser.Parser;

//Implementation on the basis that JAR migration is done for one library at a time.


public class NewAction implements IObjectActionDelegate, ListSelectionListener {

	private Shell shell;
	public static IProject selected = null;
	public static String oldjarpath;
	public static String newjarpath;
	private IProject newproject;
	
	private IProject[] AllProjects;
	public static JTextArea errors;
	
	
	static String [][] paraMethodOld;
	static String [][] paraMethodNew;
	IMethod[] jarmethodOld;
	IMethod[] jarmethodsNew;
	static String [] arrayNewJar;//number of methods in the new jar
	static String [] arrayOldJar;//number of methods in the old jar
	IMethod[] oldjFunctions;
	IMethod [] newjFunctions;
	String [] errorlist;
	public static ArrayList<String> problems;
	
	private boolean jar = false;
	
	Parser ast;
	
	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
		
	}
	
	public static IProject getproject(){
		
		return selected;
	}
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}


	
	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)	C:/Users/Philip/runtime-New_configuration/trial
	 */
	public void run(IAction action) {
		
		MessageDialog.openInformation(
				shell,
				"Migrate",
				"It is recommended that you have a backup of the selected project");
		
		
		
		selected = selected();
		
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IWorkspaceRoot root = workspace.getRoot();
	    
	    AllProjects = root.getProjects();
	
		IJavaProject javaproj = JavaCore.create(selected);
		IClasspathEntry[] rawClasspath = null;
		try {
			rawClasspath= javaproj.getRawClasspath();
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		LinkedList list = new LinkedList(java.util.Arrays.asList(rawClasspath));
	
		String[] jar = new String[rawClasspath.length];
		
		for(int i=0;i<jar.length;i++){
			IPath path = rawClasspath[i].getPath();
			
			jar[i] = path.toString();
		}
		
		
		ElementListSelectionDialog d = new ElementListSelectionDialog(shell, new LabelProvider());
		d.setElements(jar);
		d.setMultipleSelection(true);
		d.setTitle("Select one Jar file to remove");
		d.open();
		Object[] result = d.getResult();
		oldjarpath = (String)result[0];
		
		boolean flag = false;
		
			for(int i=0;i<result.length;i++)
			for(int j=0;j<list.size();j++){
			IClasspathEntry en = (IClasspathEntry) list.get(j);
			flag=en.getPath().toString().equals((String)result[i]);
				if(flag){
					list.remove(j);
				}
			}
		
	
		FileDialog fd = new FileDialog(shell);
		fd.setText("Select jar files to add");
		fd.setFilterExtensions(new String[]{"*.jar"});
		newjarpath = fd.open();
	
	
		MessageDialog.openInformation(
				shell,
				"Migrate",
				"Browsing window shown here...user browses for new jar files to be added");
		
		//-------------------
		boolean isAlreadyadded = false;
		IClasspathEntry newjar = null;
		for(IClasspathEntry en:rawClasspath){
			isAlreadyadded = en.getPath().toString().equals(newjarpath);
			if(isAlreadyadded)
				break;	
		}
		
		if(!isAlreadyadded){
				newjar = JavaCore.newLibraryEntry(new Path(newjarpath), null, null);
				list.add(newjar);
		}
		
		

		IClasspathEntry[] newclasspath = (IClasspathEntry[])list.toArray(new IClasspathEntry[0]);
		
		try {
			CloneProject(selected.getLocation().toOSString(),newclasspath);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	/*	Parser par = new Parser();//Executing the parser for now
		try {
			par.execute(null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//Compute recommendations here
		
		//create a split text pane to display the errors and recommendations
		
				String [] text = {"Errors", "Recomm. for method parameters", "Recomm. for return types","click me first"};
				JList textList = new JList(text);
				textList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				textList.setSelectedIndex(0);
				textList.addListSelectionListener((ListSelectionListener) this);
				
				
				JScrollPane listScrollPane = new JScrollPane(textList);
		         
				System.out.println("\n\n check");
				
				errors = new JTextArea();
				JScrollPane scrollPane = new JScrollPane(errors);
				JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,listScrollPane, scrollPane);
				splitPane.setOneTouchExpandable(true);
				splitPane.setDividerLocation(200);

				//Provide minimum sizes for the two components in the split pane.
				Dimension minimumSize = new Dimension(500, 350);
				listScrollPane.setMinimumSize(minimumSize);
				scrollPane.setMinimumSize(minimumSize);

				//Provide a preferred size for the split pane.
				splitPane.setPreferredSize(new Dimension(600, 400));
				JFrame frame = new JFrame();
				frame.add(splitPane);
				frame.setSize(600, 400);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		        frame.setVisible(true);
		
		
	/*	MessageDialog.openInformation(
			shell,
			"Migrate",
			"Migration Completed");
*/
	}
	
		public void CloneProject(String projectPath,IClasspathEntry[] entries) throws CoreException, IOException {		
			File source = new File(projectPath);
			File destination = new File(projectPath + "[duplicate]");
			
			if ( !source.exists() ) {
				throw new FileNotFoundException("Source project not found");
			}
			
			FileUtils.copyDirectory(source, destination); 
			IProjectDescription description;
			description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projectPath + "[duplicate]/.project"));
			String name = description.getName();

			String projectFiles = FileUtils.readFileToString(new File(projectPath + "/.project"));
			projectFiles = projectFiles.replaceAll(name, name+"[duplicate]");
			File temp = new File(projectPath + "[duplicate]/.project");
			FileUtils.writeStringToFile(temp, projectFiles);
			
			description = ResourcesPlugin.getWorkspace().loadProjectDescription(new Path(projectPath + "[duplicate]/.project"));
			newproject = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
			
			Boolean alreadyCopied = false;
			for (IProject entry : AllProjects) {
				if (newproject == entry) {
					alreadyCopied = true;
				}
			}
			
			if(alreadyCopied == false) {
				newproject.create(description, null);
				newproject.open(null);
			}
			
			IJavaProject javaProject = JavaCore.create(newproject);
			javaProject.setRawClasspath(entries, null);
			
		}
	
		public void getInfoAboutJars(IProject project, String JARFILEPATH, String[] rt_new, String[] rt_old){
			int methdInt=0;
			
			//IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath jarFilepath;
			jarFilepath= new Path(JARFILEPATH);
			String jarFilePathToString = JARFILEPATH.toString();
			IPackageFragment[] packages;

			 //Process each package 
			try{
			for ( IPackageFragment mypackage : JavaCore.create(project).getPackageFragments()) {
				// K_Binary would include also jars
				if ( mypackage.getKind() == IPackageFragmentRoot.K_BINARY ) {
					
					for ( IClassFile classFile : mypackage.getClassFiles() ) {
						if( classFile.getPath().toString().equalsIgnoreCase(jarFilePathToString) ) {
							if(jar){
								errors.append("Path of selected Jar is:\n "+ jarFilePathToString +"\n");
							}
							else{
								errors.append("Path of the Jar you would like to remove is\n: "+ jarFilePathToString);
							}
								//if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE)
				           // {
				             //   System.out.println("Source Name " + mypackage.getElementName());
				               // System.out.println("Number of Classes: " + mypackage.getClassFiles().length);
				          //  }
								errors.append("Path of the Jar you would like to remove is\n: "+ jarFilePathToString);
							//if (javaElement instanceof IType) {
							//	System.out.println("--------IType "
								//		+ javaElement.getElementName());
							for ( IJavaElement javaElement : classFile.getChildren() ) {
								if (javaElement instanceof IType) {
									errors.append("Jar class is: "+javaElement.getElementName());

									// IInitializer
									IInitializer[] inits = ((IType) javaElement).getInitializers();
									for (IInitializer init : inits) {
										errors.append("Initialize: "+init.getElementName());
									}
	 
									// IField
									IField[] fields = ((IType) javaElement).getFields();
									for (IField field : fields) {
										errors.append("Fields: "+ field.getElementName());
									}
	 
									// IMethod
									IMethod[] methods = ((IType) javaElement).getMethods();
									
									if (JARFILEPATH != oldjarpath){
										jarmethodsNew = methods;
									}
										
									else{
										jarmethodOld = methods;
									}
									
									for (IMethod method : methods) {
									
						            		/*
						            		arrayOldJar= new String[numOfMethods(selectProject, jarold)];
											String[][] paraMethodOld= new String[numOfMethods(selectProject, jarold)][];
											String []rt_old= new String[numOfMethods(selectProject, jarold)];
											
											
										arrayNewJar= new String[numOfMethods(clone, jarnew)];
						 				String [][] paraMethodNew= new String[numOfMethods(clone, jarnew)][];
										String [] rt_new= new String[numOfMethods(clone, jarnew)];
						jar=!jar;
											*/
										if(jar){
											errors.append("\nThe following returns the name of the new jar method names along with their return types:\n");
											errors.append("\nName: " + (arrayNewJar[methdInt]= method.getElementName()));
											errors.append("\nReturn Type: " + (rt_new[methdInt]= method.getReturnType()));
											paraMethodNew[methdInt]= method.getParameterTypes();
											for(int i=0; i<paraMethodNew.length;i++);
				
										}	
							
										else{
											
											errors.append("\nThe following returns the name of the removed jar method names along with their return types:\n");
											errors.append("Name: " + (arrayOldJar[methdInt]= method.getElementName()));				
											errors.append("\nReturn Type: " + 	(rt_old[methdInt]= method.getReturnType()));
											paraMethodOld[methdInt]= method.getParameterTypes();
											for(int i=0; i<paraMethodOld.length;i++);
												
										methdInt++;
									}
								}
							}
						}
					}
				}
			}
			}
		 }//end of try
	catch (JavaModelException e) {
	 e.printStackTrace();	}
		}//end of method	
		
		
	static private void importProject(IPath p)throws CoreException{
	
		IProjectDescription des = ResourcesPlugin.getWorkspace().loadProjectDescription(p) ;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(des.getName());
		project.create(des,null);
		project.open(null);
	}
	
	public void gettingMethodCalls (IProject iproject, String jarpath) throws JavaModelException{
		IPath path = new Path(jarpath);
		IPackageFragment[] packages = JavaCore.create(iproject).getPackageFragments();
	}
	
	
	private IProject selected(){
		IProject project = null;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
		    {
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	        Object firstElement = selection.getFirstElement();
	        if (firstElement instanceof IAdaptable)
		    {
	            project = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
	            
	        }
		}
		return project;
	}
	
	public int numOfMethods(IProject iproject, String jarFilepath) {
		int methodsnum=0;	
		IPath JarFilepath;
		JarFilepath =new Path(jarFilepath);
		String jarFilePathToString = jarFilepath.toString();
			
		try{
			
		for ( IPackageFragment mypackage : JavaCore.create(iproject).getPackageFragments() ) {
			if ( mypackage.getKind() == IPackageFragmentRoot.K_BINARY ) {
				for ( IClassFile classFile : mypackage.getClassFiles() ) {
					if( classFile.getPath().toString().equalsIgnoreCase(jarFilePathToString) ) {
						for ( IJavaElement javaElement : classFile.getChildren() ) {
							if (javaElement instanceof IType) {
					
								IMethod[] methods = ((IType) javaElement).getMethods();
								for (IMethod method : methods) {
									methodsnum++;
								}
							}
						}
					}
				}
			}
		}
	}
	catch (JavaModelException e) {
	   e.printStackTrace();	}
		
		return methodsnum;
		
	}	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int methdInt;
		
		problems = new ArrayList<String>();
		
		// TODO Auto-generated method stub
		if (e.getValueIsAdjusting())
            return;
 
        JList theList = (JList)e.getSource();
        
        if (theList.isSelectionEmpty()) {
            errors.setText("Nothing selected.");
        } else {
            int index = theList.getSelectedIndex();
            
            if(index == 0){
            	errors.setText("The following list the errors in the project: "+"\n\n");
            	 //String [][] paraMethodOld;
				 //String [][] paraMethodNew;
				// String [] rt_old;
				// String [] rt_new;
            	try {
            		
     
					arrayOldJar= new String[numOfMethods(selected, oldjarpath)];
					String[][] paraMethodOld= new String[numOfMethods(selected, oldjarpath)][];
					String []rt_old= new String[numOfMethods(selected, oldjarpath)];
					String [][] paraMethodNew= new String[numOfMethods(newproject, newjarpath)][];
					String [] rt_new= new String[numOfMethods(newproject, newjarpath)];
					methdInt=0;
					getInfoAboutJars(selected, oldjarpath,rt_new,rt_old);
					System.out.println("old jar has this many methods: "+methdInt);
					arrayNewJar= new String[numOfMethods(newproject, newjarpath)];
					
					jar=!jar;
					methdInt=0;
					getInfoAboutJars(newproject, newjarpath,rt_new,rt_old); 
					System.out.println("the current jar has this many methods: "+methdInt);
					
				
					ast = new Parser(errors);
            		ast.analyseMethods(newproject);
            		
            		
            		System.out.print(problems.size());
            		errors.append("\n______________________________________________________\n");
            		errors.append("Overall Information:\n");
            		errors.append("The total number of errors in you project are: "+ problems.size());
            		for(int i=0; i< problems.size(); i++)
            			errors.append("\nThe type of error is: "+problems.get(i).toString());
            		errors.append("\nPlease choose one of the recommendations tab to see how to fix your error!");
//            	
            	} catch (JavaModelException e1) {
            		// TODO Auto-generated catch block
            		e1.printStackTrace();
            	}
            }
            else if(index ==1){
            	errors.setText("Displays all the method parameters: \n");
            	try {
					APIRecommend r = new APIRecommend(jarmethodOld, jarmethodsNew, problems);
					ArrayList<String> get = r.getMethodParameters();
					String parameters = r.getParameters();
					errors.append(parameters);
					
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }else if(index ==2){
            	errors.setText("Displays all the return types: \n");
            	try {
            		APIRecommend r = new APIRecommend(jarmethodOld, jarmethodsNew, problems);
            		ArrayList<String> get = r.getMethodParameters();//Exception here
					String returnType = r.getReturnType();
					errors.append(returnType);
				} catch (JavaModelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }else if(index ==3){
            	errors.setText("can't believe this is happening");
            }
        }
	}
	
	   
	          

}
