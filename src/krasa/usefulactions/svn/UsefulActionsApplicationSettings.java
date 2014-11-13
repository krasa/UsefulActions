package krasa.usefulactions.svn;

public class UsefulActionsApplicationSettings {

	private Integer version = 2;
	private String svnAddress;
	private boolean showSvnBrowseButton = true;
	private String recentProjectsSize = "50";
	private String rebuildDelay;

	public String getSvnAddress() {
		return svnAddress;
	}

	public void setSvnAddress(final String svnAddress) {
		this.svnAddress = svnAddress;
	}

	public boolean isShowSvnBrowseButton() {
		return showSvnBrowseButton;
	}

	public void setShowSvnBrowseButton(final boolean showSvnBrowseButton) {
		this.showSvnBrowseButton = showSvnBrowseButton;
	}

	public String getRecentProjectsSize() {
		return recentProjectsSize;
	}

	public void setRecentProjectsSize(final String recentProjectsSize) {
		this.recentProjectsSize = recentProjectsSize;
	}

	public String getRebuildDelay() {
		return rebuildDelay;
	}

	public void setRebuildDelay(String rebuildDelay) {
		this.rebuildDelay = rebuildDelay;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
