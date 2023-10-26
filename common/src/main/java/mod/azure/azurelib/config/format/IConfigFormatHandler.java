package mod.azure.azurelib.config.format;

public interface IConfigFormatHandler {

    IConfigFormat createFormat();

    String fileExt();
}
