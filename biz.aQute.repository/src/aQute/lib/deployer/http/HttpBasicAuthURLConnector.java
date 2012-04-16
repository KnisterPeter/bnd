package aQute.lib.deployer.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.regex.*;

import aQute.bnd.service.*;
import aQute.bnd.service.url.*;
import aQute.lib.base64.*;
import aQute.lib.io.*;
import aQute.libg.glob.*;
import aQute.libg.reporter.*;

public class HttpBasicAuthURLConnector implements URLConnector, Plugin {
	
	private static final String PREFIX_PATTERN = "pattern.";
	private static final String PREFIX_USER = "uid.";
	private static final String PREFIX_PASSWORD = "pwd.";

	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String PREFIX_BASIC_AUTH = "Basic ";
	
	private static final String HEADER_IF_NONE_MATCH = "If-None-Match";
	private static final String HEADER_ETAG = "ETag";
	private static final int RESPONSE_NOT_MODIFIED = 304;

	private static class Mapping {
		Glob urlPattern;
		String user;
		String pass;
		Mapping(Glob urlPattern, String user, String pass) {
			this.urlPattern = urlPattern; this.user = user; this.pass = pass;
		}
	}

	private final AtomicBoolean inited = new AtomicBoolean(false);
	private final List<Mapping> mappings = new LinkedList<Mapping>();
	
	private Reporter reporter;
	private String configFileList;
	
	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}
	
	public void setProperties(Map<String, String> map) {
		configFileList = map.get("configs");
		if (configFileList == null)
			throw new IllegalArgumentException("'configs' must be specified on HttpBasicAuthURLConnector");
	}
	
	protected void init() {
		if (inited.compareAndSet(false, true)) {
			mappings.clear();
			
			StringTokenizer tokenizer = new StringTokenizer(configFileList, ",");
			while (tokenizer.hasMoreTokens()) {
				String configFileName = tokenizer.nextToken().trim();
				
				File file = new File(configFileName);
				if (file.exists()) {
					Properties props = new Properties();
					InputStream stream = null;
					try {
						stream = new FileInputStream(file);
						props.load(stream);
						
						for (Object key : props.keySet()) {
							String name = (String) key;
							
							if (name.startsWith(PREFIX_PATTERN)) {
								String id = name.substring(PREFIX_PATTERN.length());
								
								Glob glob = new Glob(props.getProperty(name));
								String uid = props.getProperty(PREFIX_USER + id);
								String pwd = props.getProperty(PREFIX_PASSWORD + id);
								
								mappings.add(new Mapping(glob, uid, pwd));
							}
						}
					} catch (IOException e) {
						reporter.error("Failed to load %s", configFileName);
					} finally {
						if (stream != null) IO.close(stream);
					}
				}
			}
		}
	}

	public InputStream connect(URL url) throws IOException {
		TaggedData data = connectTagged(url, null);
		if (data == null)
			throw new IOException("HTTP server did not respond with data.");
		
		return data.getInputStream();
	}

	public TaggedData connectTagged(URL url) throws IOException {
		return connectTagged(url, null);
	}
	
	public TaggedData connectTagged(URL url, String tag) throws IOException {
		init();
		
		for (Mapping mapping : mappings) {
			Matcher matcher = mapping.urlPattern.matcher(url.toString());
			if (matcher.find())
				return connectTagged(url, tag, mapping.user, mapping.pass);
		}
		
		return connectTagged(url, tag, null, null);
	}

	private TaggedData connectTagged(URL url, String tag, String user, String pass) throws IOException {
		TaggedData result;
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Add the authorization string using HTTP Basic Auth
		if (user != null && pass != null) {
			String authString = user + ":" + pass;
			String encoded = Base64.encodeBase64(authString.getBytes());
			connection.setRequestProperty(HEADER_AUTHORIZATION, PREFIX_BASIC_AUTH + encoded);
		}
		
		// Add the ETag
		if (tag != null)
			connection.setRequestProperty(HEADER_IF_NONE_MATCH, tag);
		
		connection.connect();
		
		int responseCode = connection.getResponseCode();
		if (responseCode == RESPONSE_NOT_MODIFIED)
			result = null;
		else {
			String responseTag = connection.getHeaderField(HEADER_ETAG);
			result = new TaggedData(responseTag, connection.getInputStream());
		}
		
		return result;
	}
	
}