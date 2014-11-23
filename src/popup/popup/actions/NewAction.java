package popup.popup.actions;


import java.awt.List;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;

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




public class NewAction implements IObjectActionDelegate {

	private Shell shell;
	
	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
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
		
		
		
		IProject selected = selected();
		//view libraries in it
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
		d.setTitle("Select Jar files to remove");
		d.open();
		Object[] result = d.getResult();
		
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
		String s = fd.open();
		System.out.println(s);
	/*	
		MessageDialog.openInformation(
				shell,
				"Migrate",
				"Browsing window shown here...user browses for new jar files to be added");
		
		//String[] jarpath = null;//This path is given by the user, it should be an array,...commented for now.
		//-------------------
		String jarpath="C:/Program Files/Java/jre1.8.0_20/lib/management-agent.jar";
		boolean isAlreadyadded = false;
		IClasspathEntry newjar = null;
		for(IClasspathEntry en:rawClasspath){
			isAlreadyadded = en.getPath().toString().equals(jarpath);
			if(isAlreadyadded)
				break;	
		}
		
		if(!isAlreadyadded){
				newjar = JavaCore.newLibraryEntry(new Path(jarpath), null, null);
				list.add(newjar);
		}
		*/
		IClasspathEntry[] newclasspath = (IClasspathEntry[])list.toArray(new IClasspathEntry[0]);
		try {
			javaproj.setRawClasspath( newclasspath, null);
		} catch (JavaModelException e) {
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
