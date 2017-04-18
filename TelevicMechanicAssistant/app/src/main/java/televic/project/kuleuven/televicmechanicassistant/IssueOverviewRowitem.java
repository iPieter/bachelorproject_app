package televic.project.kuleuven.televicmechanicassistant;

/**
 * Created by Matthias on 15/04/2017.
 */

public class IssueOverviewRowitem {
    private String workplace;
    private String status;
    private String traincoach;
    private String description;

    public IssueOverviewRowitem(){

    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTraincoach() {
        return traincoach;
    }

    public void setTraincoach(String traincoach) {
        this.traincoach = traincoach;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
