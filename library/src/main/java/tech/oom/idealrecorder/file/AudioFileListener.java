package tech.oom.idealrecorder.file;

/**
 * Created by issuser on 2017/6/8 0008.
 */

public interface AudioFileListener {
    /**
     * 文件保存失败
     * @param reason 失败的原因
     */
    void onFailure(String reason);

    /**
     * 文件保存成功
     * @param savePath 保存文件的路径
     */
    void onSuccess(String savePath);
}
