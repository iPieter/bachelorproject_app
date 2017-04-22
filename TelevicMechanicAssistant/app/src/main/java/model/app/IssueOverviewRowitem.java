package model.app;

import java.io.Serializable;

import model.Workplace;
import model.issue.Issue;

/**
 * Created by Matthias on 15/04/2017.
 */

public class IssueOverviewRowitem  implements Serializable {
    private Issue issue;
    private Workplace workplace;

    public IssueOverviewRowitem(){

    }

    //Getters&Setters
    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Workplace getWorkplace() {
        return workplace;
    }

    public void setWorkplace(Workplace workplace) {
        this.workplace = workplace;
    }
}
