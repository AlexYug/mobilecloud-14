package org.magnum.dataup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.hibernate.annotations.common.util.impl.Log;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.VideoFileManager;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse; 
import javax.servlet.http.HttpServletRequest;

@Controller
public class VideoController{

	private Collection<Video> mVideos = new java.util.Vector<Video>();
	private AtomicLong mNextId = new AtomicLong(0);
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return mVideos;
	}

	private String getUrlBaseForLocalServer() {
		   HttpServletRequest request = 
		       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		   String base = 
		      "http://"+request.getServerName() 
		      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		   return base;
		}
	private String getUrlBaseForLocalServer(HttpServletRequest request) {
		   String base = 
		      "http://"+request.getServerName() 
		      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		   return base;
		}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v, HttpServletRequest request) {
		v.setId(mNextId.incrementAndGet());
		v.setDataUrl(getUrlBaseForLocalServer(request) + VideoSvcApi.VIDEO_SVC_PATH + Long.toString(v.getId()) + '/' + VideoSvcApi.DATA_PARAMETER);
		mVideos.add(v);
		return v;
	}

	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile multyPartFile,
			HttpServletResponse rawResponse,
			HttpServletRequest rawRequest) throws IOException {
		Long idWrapper = new Long(id);
		
		Video videoToStore = null;
		for(Video video : mVideos)
		{
			if(video.getId() == id)
			{
				videoToStore = video;
			}
		}
		if(videoToStore == null)
		{
			rawResponse.sendError(404, "There is no video entry with Id=" + idWrapper.toString());
			return null;
		}
		VideoFileManager videoFileManager = null;
		try
		{
			videoFileManager = VideoFileManager.get();
		}
		catch(IOException e)
		{
			return null;
		}
		InputStream videoData = multyPartFile.getInputStream();		
		try
		{
			videoFileManager.saveVideoData(videoToStore, videoData);
		}
		catch(IOException e)
		{
			return null;
		}
		
		VideoStatus status = new VideoStatus(VideoStatus.VideoState.READY);
		return status;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void getData(@PathVariable(
			VideoSvcApi.ID_PARAMETER) long id,
			HttpServletResponse rawResponse) throws IOException {
		Video requestedVideo = null;
		for(Video video: mVideos)
		{
			if(id == video.getId())
			{
				requestedVideo = video;
			}
		}
		Long idWrapper = new Long(id);
		if(requestedVideo == null)
		{
			rawResponse.sendError(404, "There is no video with Id:" + idWrapper.toString());
			return;
		}
		OutputStream result = rawResponse.getOutputStream();
		VideoFileManager videoFileManager = null;
		try
		{
			videoFileManager = VideoFileManager.get();
		}
		catch(IOException e)
		{
			rawResponse.sendError(500);
			return;
		}
		videoFileManager.copyVideoData(requestedVideo, result);
		
		return;
	}

}
