package com.tutorial.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import com.tutorial.util.ResourceUtils;

/**
 * Abstract base class for resources which resolve URLs into File references,
 * such as {@link UrlResource} or {@link ClassPathResource}.
 *
 * <p>Detects the "file" protocol as well as the JBoss "vfs" protocol in URLs,
 * resolving file system references accordingly.
 *
 * @author Juergen Hoeller
 * @since 3.0
 */
public abstract class AbstractFileResolvingResource extends AbstractResource {

	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URL, String)
	 */
	@Override
	public File getFile() throws IOException {
		URL url = getURL();
		if(url.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(url).getFile();
		}
		return ResourceUtils.getFile(url, getDescription());
	}
	
	/**
	 * This implementation determines the underlying File
	 * (or jar file, in case of a resource in a jar/zip).
	 */
	@Override
	protected File getFileForLastModifiedCheck() throws IOException {
		URL url = getURL();
		if(ResourceUtils.isJarURL(url)) {
			 URL actualUrl = ResourceUtils.extractJarFileURL(url);
			 if(actualUrl.getProtocol().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
				 return VfsResourceDelegate.getResource(actualUrl).getFile();
			 }
			 return ResourceUtils.getFile(actualUrl, "Jar URL");
		}
		else {
			return getFile();
		}
	}
	
	/**
	 * This implementation returns a File reference for the underlying class path
	 * resource, provided that it refers to a file in the file system.
	 * @see org.springframework.util.ResourceUtils#getFile(java.net.URI, String)
	 */
	protected File getFile(URI uri) throws IOException {
		if(uri.getScheme().startsWith(ResourceUtils.URL_PROTOCOL_VFS)) {
			return VfsResourceDelegate.getResource(uri).getFile();
		}
		return ResourceUtils.getFile(uri, getDescription());
	}
	
	@Override
	public boolean exists() {
		try {
			URL url = getURL();
			if(ResourceUtils.isFileURL(url)) {
				//Proceed with file system resolution...
				return getFile().exists();
			}
			else {
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				HttpURLConnection httpCon = 
						(con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
				if(httpCon != null) {
					httpCon.setRequestMethod("HEAD");
					int code = httpCon.getResponseCode();
					if(code == HttpURLConnection.HTTP_OK) {
						return true;
					} 
					else if(code == HttpURLConnection.HTTP_NOT_FOUND) {
						return false;
					}
				}
				if(con.getContentLength() >= 0) {
					return true;
				}
				if(httpCon != null) {
					httpCon.disconnect();
					return false;
				}
				else {
					//Fall back to stream existence : can we open the stream?
					InputStream is = getInputStream();
					is.close();
					return true;
				}
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public boolean isReadable() {
		try {
			URL url = getURL();
			if(ResourceUtils.isFileURL(url)) {
				File file = getFile();
				return (file.canRead() && !file.isDirectory());
			} 
			else {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public long contentLength() throws IOException {
		URL url = getURL();
		if(ResourceUtils.isFileURL(url)) {
			//Proceed with file system resolution....
			return super.contentLength();
		} else {
			//Try a URL connection content-length header....
			URLConnection con = url.openConnection();
			con.setUseCaches(false);
			if(con instanceof HttpURLConnection) {
				((HttpURLConnection) con).setRequestMethod("HEAD");
			}
			return con.getContentLength();
		}
	}
	
	@Override
	public long lastModified() throws IOException {
		URL url = getURL();
		if(ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
			//Proceed with file system resolution...
			return super.lastModified();
		}
		else {
			// Try a URL connection last-modified header...
			URLConnection con = url.openConnection();
			con.setUseCaches(false);
			if(con instanceof HttpURLConnection) {
				((HttpURLConnection) con).setRequestMethod("HEAD");
			}
			return con.getLastModified();
		}
	}
	
	private static class VfsResourceDelegate {
		
		public static Resource getResource(URL url) throws IOException {
			return new VfsResource(VfsUtils.getRoot(url));
		}
		
		public static Resource getResource(URI uri) throws IOException {
			return new VfsResource(VfsUtils.getRoot(uri));
		}
	}

}
