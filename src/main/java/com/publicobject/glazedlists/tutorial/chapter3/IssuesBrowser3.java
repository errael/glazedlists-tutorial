package com.publicobject.glazedlists.tutorial.chapter3;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.issuezilla.Issue;
import ca.odell.issuezilla.IssuezillaXMLParser;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IssuesBrowser3 {

    /** event list that hosts the issues */
    private EventList<Issue> issuesEventList = new BasicEventList<Issue>();

    /**
     * Create an IssueBrowser for the specified issues.
     */
    public IssuesBrowser3(Collection<Issue> issues) {
        issuesEventList.addAll(issues);
    }

    // tag::Display[]
    /**
     * Display a frame for browsing issues.
     */
    public void display() {
        SortedList<Issue> sortedIssues = new SortedList<Issue>(issuesEventList, new IssueComparator());
        JTextField filterEdit = new JTextField(10);
        FilterList<Issue> textFilteredIssues = new FilterList<Issue>(sortedIssues, new TextComponentMatcherEditor<Issue>(filterEdit, new IssueTextFilterator()));

        // create a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        AdvancedTableModel<Issue> issuesTableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(textFilteredIssues, new IssueTableFormat());
        JTable issuesJTable = new JTable(issuesTableModel);
        TableComparatorChooser<Issue> tableSorter = TableComparatorChooser.install(issuesJTable, sortedIssues, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable);
        panel.add(new JLabel("Filter: "), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(filterEdit,             new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(issuesTableScrollPane,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

        // create a frame with that panel
        JFrame frame = new JFrame("Issues");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(540, 380);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }
	// end::Display[]

    /**
     * Launch the IssuesBrowser from the commandline.
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: IssuesBrowser <file>");
            return;
        }

        // load some issues
        final Collection<Issue> issues;
        try {
            IssuezillaXMLParser parser = new IssuezillaXMLParser();
            InputStream issuesInStream = new FileInputStream(args[0]);
            issues = parser.loadIssues(issuesInStream, null);
            issuesInStream.close();
        } catch(IOException e) {
            e.printStackTrace();
            return;
        }

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // create the browser
                IssuesBrowser3 browser = new IssuesBrowser3(issues);
                browser.display();
            }
        });
    }
}
