package cn.yyzmain.result;

public class MainResultGenerator {


    public static <T> MainResult<T> createResult(Integer code, String msg, T data) {

        MainResult<T> result = MainResult.newInstance();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> MainResult<T> createOkResult() {
        return createResult(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    /**
     * 成功，默认提示
     */
    public static <T> MainResult<T> createOkResult(T data) {
        return createResult(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }


    /**
     * 成功，自定义提示
     */
    public static <T> MainResult<T> createOkResult(String msg, T data) {
        return createResult(ResultCode.SUCCESS.getCode(), msg, data);
    }

    //////////////////////////////////  失败
    /**
     * 失败，默认提示
     */
    public static <T> MainResult<T> createFailResult() {
        return createResult(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMsg(), null);
    }

    /**
     * 失败，自定义提示
     */
    public static <T> MainResult<T> createFailResult(String msg) {
        return createResult(ResultCode.ERROR.getCode(), msg, null);
    }

    /**
     * 失败，自定义提示 & 返回失败详情
     */
    public static <T> MainResult<T> createFailResult(Integer code, String msg, T data) {
        return createResult(code, msg, data);
    }

    /**
     * 失败，自定义提示
     */
    public static <T> MainResult<T> createFailResult(Integer code, String msg) {
        return createResult(code, msg, null);
    }


}
