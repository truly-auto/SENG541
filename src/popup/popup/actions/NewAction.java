package popup.popup.actions;


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

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
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




public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	public static IProject selected = null;
	public String oldjarpath;
	public String newjarpath;
	private IProject newproject;
	
	private IProject[] AllProjects;
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
		
		//Comment code below to test parser without changing jar
		IJavaProject javaproj = JavaCore.create(selected);
		IClasspathEntry[] rawClasspath = null;
		try {
			rawClasspath= javaproj.getRawClasspath();
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		LinkedList list = new LinkedList(java.util.Arrays.asList(rawClasspath));
	
		//System.out.println(rawClasspath.length);
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
		fd.setText("Select jar files to remove");
		fd.setFilterExtensions(new String[]{"*.jar"});
		newjarpath = fd.open();
	
	
		MessageDialog.openInformation(
				shell,
				"Migrate",
				"Browsing window shown here...user browses for new jar files to be added");
		
		//String[] jarpath = null;//This path is given by the user, it should be an array,...commented for now.
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
		
		
		
		//Comment till here to test parser without executing jar migrate 
		Parser par = new Parser();//not sure how to execute the parser class
		try {
			par.execute(null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//option to delete and add more libraries
		
		//C:/Users/Philip/Downloads
		
		MessageDialog.openInformation(
			shell,
			"Migrate",
			"Migration Completed");
		
		
	}
	
	
	//newprojects the project with the specified path selected by the user. 
		public void CloneProject(String projectPath,IClasspathEntry[] entries) throws CoreException, IOException {		
			File source = new File(projectPath);
			File destination = new File(projectPath + "[duplicate]");
			
			if ( !source.exists() ) {
				throw new FileNotFoundException("Source project not found");
			}
			
			FileUtils.copyDirectory(source, destination); 
			System.out.println("here - checkpoint 1");
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
			
			System.out.println("here - checkpoint 2");
			IJavaProject javaProject = JavaCore.create(newproject);
			javaProject.setRawClasspath(entries, null);
			
			System.out.println("here - checkpoint 3");
		
		}
	
	static private void importProject(IPath p)throws CoreException{
	
		IProjectDescription des = ResourcesPlugin.getWorkspace().loadProjectDescription(p) ;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(des.getName());
		project.create(des,null);
		project.open(null);
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
	   
	          

}
