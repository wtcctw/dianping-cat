package com.dianping.cat.alarm.spi.dx.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class MtHttpUtil {
	private static HttpClient httpClient = new DefaultHttpClient(new PoolingClientConnectionManager());

	public static <T> T get(String url, Class<T> returnType, String... headers) {
		return execute(new HttpGet(url), returnType, headers);
	}

	public static <T> T delete(String url, Class<T> returnType, String... headers) {
		return execute(new HttpDelete(url), returnType, headers);
	}

	public static <T> T put(String url, Object data, Class<T> returnType, String... headers) {
		try {
			HttpPut put = new HttpPut(url);
			StringEntity body = new StringEntity(JSON.toJSONString(data), "utf-8");
			body.setContentType("Content-Type: application/json; charset=utf-8");
			put.setEntity(body);
			put.setHeader("Content-Type", "application/json; charset=utf-8");
			return execute(put, returnType, headers);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
		}
	}

	public static <T> T put(String url, Object data, Class<T> returnType, int socket_timeout, int conn_timeout,
	      String... headers) {
		try {
			HttpPut put = new HttpPut(url);
			HttpParams params = new BasicHttpParams();
			if (socket_timeout != 0 && conn_timeout != 0) {
				params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socket_timeout);
				params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, conn_timeout);
				put.setParams(params);
			}
			StringEntity body = new StringEntity(JSON.toJSONString(data), "utf-8");
			body.setContentType("Content-Type: application/json; charset=utf-8");
			put.setEntity(body);
			put.setHeader("Content-Type", "application/json; charset=utf-8");
			return execute(put, returnType, headers);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
		}
	}

	public static <T> T post(String url, Object data, Class<T> returnType, String... headers) {
		try {
			HttpPost put = new HttpPost(url);
			String contentString = JSON.toJSONString(data);
			StringEntity body = new StringEntity(contentString, "utf-8");
			body.setContentType("Content-Type: application/json; charset=utf-8");
			put.setEntity(body);
			put.setHeader("Content-Type", "application/json; charset=utf-8");
			return execute(put, returnType, headers);
		} catch (UnsupportedEncodingException e) {
			throw new HttpException(506, "UnsupportedEncodingException(utf-8)", e);
		}
	}

	public static void post(String url, ByteArrayBody[] files, String... headers) {
		HttpPost post = new HttpPost(url);

		MultipartEntity body = new MultipartEntity();
		for (ByteArrayBody file : files) {
			body.addPart("file", file);
		}
		post.setEntity(body);

		execute(post, Object.class);
	}

	public static void put(String url, Object data, String... headers) {
		put(url, data, Object.class, headers);
	}

	public static void post(String url, Object data, String... headers) {
		post(url, data, Object.class, headers);
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(HttpUriRequest req, Class<T> returnType, String... headers) {

		try {
			// 插入basic验证等基础逻辑
			for (int i = 0; i < headers.length; i += 2) {
				req.addHeader(headers[i], headers[i + 1]);
			}
			HttpResponse jres = httpClient.execute(req);

			HttpEntity ent = jres.getEntity();
			String content = ent != null ? EntityUtils.toString(jres.getEntity(), "UTF-8") : null;
			if (jres.getStatusLine().getStatusCode() >= 200 && jres.getStatusLine().getStatusCode() < 300) {
				JSONObject json = JSON.parseObject(content);

				if (json.get("error") == null) {
					if (returnType.isArray()) {
						List<?> list = JSON.parseArray(json.getJSONArray("data").toJSONString(),
						      returnType.getComponentType());
						return (T) list.toArray((T[]) java.lang.reflect.Array.newInstance(returnType.getComponentType(),
						      list.size()));
					} else {
						if (returnType == Long.class) {
							return (T) json.getLong("data");
						} else {
							return JSON.parseObject(json.getJSONObject("data").toJSONString(), returnType);
						}
					}
				} else {
					MtError error = JSON.parseObject(json.getJSONObject("error").toJSONString(), MtError.class);
					throw new HttpException(error.code, error.type, error.message);
				}
				//
			} else {
				throw new HttpException(jres.getStatusLine().getStatusCode(), jres.getStatusLine().getReasonPhrase(),
				      content);
			}
		} catch (IOException e) {
			throw new HttpException(505, "IOException", e);
		}
	}

	public static class MtResponse<T> {
		private T data;

		private MtError error;

		public T getData() {
			return data;
		}

		public void setData(T data) {
			this.data = data;
		}

		public MtError getError() {
			return error;
		}

		public void setError(MtError error) {
			this.error = error;
		}
	}

	public static class MtError {
		private int code;

		private String type;

		private String message;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

	}
}