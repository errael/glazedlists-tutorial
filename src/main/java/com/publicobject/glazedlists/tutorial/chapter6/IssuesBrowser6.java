package com.publicobject.glazedlists.tutorial.chapter6;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.AdvancedTableModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.issuezilla.Issue;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IssuesBrowser6 {

    /** reads issues from a stream and populates the issues event list */
    private IssuesLoader issueLoader = new IssuesLoader();

    /** event list that hosts the issues */
    private EventList<Issue> issuesEventList = issueLoader.getIssues();

    /**
     * Create an IssueBrowser for the specified issues.
     */
    public IssuesBrowser6(String file) {
        issueLoader.load(file);
    }

    /**
     * Display a frame for browsing issues. This should only be run on the Swing
     * event dispatch thread.
     */
    public void display() {
        issuesEventList.getReadWriteLock().readLock().lock();
        try {
            // create the transformed models
            SortedList<Issue> sortedIssues = new SortedList<Issue>(issuesEventList, new IssueComparator());
            UsersSelect usersSelect = new UsersSelect(sortedIssues);
            FilterList<Issue> userFilteredIssues = new FilterList<Issue>(sortedIssues, usersSelect);
            JTextField filterEdit = new JTextField(10);
            FilterList<Issue> textFilteredIssues = new FilterList<Issue>(userFilteredIssues, new TextComponentMatcherEditor<Issue>(filterEdit, new IssueTextFilterator()));

            // create the issues table
            AdvancedTableModel<Issue> issuesTableModel = GlazedListsSwing.eventTableModelWithThreadProxyList(textFilteredIssues, new IssueTableFormat());
            JTable issuesJTable = new JTable(issuesTableModel);
            TableComparatorChooser<Issue> tableSorter = TableComparatorChooser.install(issuesJTable, sortedIssues, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
            JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable);

            // create the users list
            JScrollPane usersListScrollPane = new JScrollPane(usersSelect.getJList());

            // create the panel
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.add(new JLabel("Filter: "), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
            panel.add(filterEdit, new GridBagConstraints(0, 1, 1, 1, 0.15, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
            panel.add(new JLabel("Reported By: "), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
            panel.add(usersListScrollPane, new GridBagConstraints(0, 3, 1, 1, 0.15, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
            panel.add(issuesTableScrollPane, new GridBagConstraints(1, 0, 1, 4, 0.85, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

            // create a frame with that panel
            JFrame frame = new JFrame("Issues");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(540, 380);
            frame.getContentPane().add(panel);
            frame.setVisible(true);
        } finally {
            issuesEventList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Launch the IssuesBrowser from the commandline.
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: IssuesBrowser <file>");
            return;
        }

        // create the browser and start loading issues
        final IssuesBrowser6 browser = new IssuesBrowser6(args[0]);

        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                browser.display();
            }
        });

    }
}
