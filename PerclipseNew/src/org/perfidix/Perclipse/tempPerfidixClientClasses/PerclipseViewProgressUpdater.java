package org.perfidix.Perclipse.tempPerfidixClientClasses;

import java.util.List;

import org.eclipse.jdt.core.IType;

public class PerclipseViewProgressUpdater {
	
	private PerclipseViewStub viewStub;
	
	public PerclipseViewProgressUpdater(String host, int port){
		if(host=="" || host==null)
			host="localhost";
		viewStub= new PerclipseViewStub(host,port);
	}
	
	public void initProgressView(int totalRuns, List<?> elementNameAndTotalBenchs){
		if(elementNameAndTotalBenchs!=null){
			Object[] elementArray= elementNameAndTotalBenchs.toArray();
			viewStub.initTotalBenchProgress(totalRuns, elementArray);			
		}
	}
	public void updateCurrentElement(IType element){
		if(element!=null){
			viewStub.updateCurrentRun(element.getFullyQualifiedName());
		}
	}
	public void updateErrorInElement(IType element){
		if(element!=null){
			viewStub.updateError(element.getFullyQualifiedName());
		}
	}
	public void finished(){
		viewStub.finishedBenchRuns();
	}

}
