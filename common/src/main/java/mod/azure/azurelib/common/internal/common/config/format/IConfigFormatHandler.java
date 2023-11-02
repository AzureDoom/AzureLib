package mod.azure.azurelib.common.internal.common.config.format;

public interface IConfigFormatHandler {

    IConfigFormat createFormat();

    String fileExt();
}
