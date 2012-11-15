package br.ufpe.cin.emergo.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class DFA4SPLPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {

	private FileFieldEditor alloyFileFieldEditor;
	private Text mainClass;

	public DFA4SPLPropertyPage() {
		super();
	}

	/**
	 * Creates the contents showed in the property page.
	 * 
	 */
	protected Control createContents(Composite parent) {
		Composite myComposite = new Composite(parent, SWT.NONE);
		GridLayout mylayout = new GridLayout();
		mylayout.marginHeight = 1;
		mylayout.marginWidth = 1;
		myComposite.setLayout(mylayout);

		alloyFileFieldEditor = new FileFieldEditor("TESTE", "Feature model alloy file: ", myComposite);
		alloyFileFieldEditor.setStringValue(getAlloyFilePath());

		Label procedureTypeLabel = new Label(myComposite, SWT.NONE);
		procedureTypeLabel.setLayoutData(new GridData());
		procedureTypeLabel.setText("Main class: ");
		mainClass = new Text(myComposite, SWT.BORDER);
		mainClass.setText(getMainClass());
		
		return myComposite;
	}

	protected String getMainClass() {
		IResource resource = (IResource) getElement().getAdapter(IResource.class);
		return DFA4SPLProperties.getMainClass(resource);
	}

	protected void setMainClass(String mainClass) {
		IResource resource = (IResource) getElement().getAdapter(IResource.class);
		DFA4SPLProperties.setMainClass(resource, mainClass);
	}

	protected String getAlloyFilePath() {
		IResource resource = (IResource) getElement().getAdapter(IResource.class);
		return DFA4SPLProperties.getAlloyFilePath(resource);
	}

	protected void setAlloyFilePath(String alloyFilePath) {
		IResource resource = (IResource) getElement().getAdapter(IResource.class);
		DFA4SPLProperties.setAlloyFilePath(resource, alloyFilePath);
	}
	
	public boolean performOk() {
		setAlloyFilePath(alloyFileFieldEditor.getStringValue());
		setMainClass(mainClass.getText());
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		//XXX: Implement fall back to default values
	}

}