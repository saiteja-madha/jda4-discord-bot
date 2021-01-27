package bot.data;

public enum ImageType {

    IMAGE(".png"),
    GIF(".gif");

    private final String fileExtension;

    ImageType(String fileName) {
        this.fileExtension = fileName;
    }

    public String getFileName() {
        return "discord_bot" + +'_' + System.currentTimeMillis() + fileExtension;
    }

}
