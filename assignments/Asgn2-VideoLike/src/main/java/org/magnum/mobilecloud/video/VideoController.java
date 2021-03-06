/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VideoController {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	// The VideoRepository that we are going to store our videos
	// in. We don't explicitly construct a VideoRepository, but
	// instead mark this object as a dependency that needs to be
	// injected by Spring. Our Application class has a method
	// annotated with @Bean that determines what object will end
	// up being injected into this member variable.
	//
	// Also notice that we don't even need a setter for Spring to
	// do the injection.
	//
	@Autowired
	private VideoRepository mVideos;

//	@RequestMapping(value="/go",method=RequestMethod.GET)
//	public @ResponseBody String goodLuck(){
//		return "Good Luck!";
//	}
	
	@RequestMapping(value="/video/{id}/like", method=RequestMethod.POST)
	void likeVideo(
			@PathVariable("id") long id,
			Principal p,
			HttpServletResponse rawResponse) throws IOException
	{
		Video video = null;
		try
		{
			video = findVideo(id);
		}
		catch(java.lang.Exception e)
		{
			rawResponse.sendError(404);
			return;
		}
		if(video == null)
		{
			rawResponse.sendError(404);
			return;
		}
		if(video.likeByUser(p.getName())){
			rawResponse.sendError(200);
			mVideos.save(video);
		}
		else{
			rawResponse.sendError(400);
		}
	}

	@RequestMapping(value="/video/{id}/unlike", method=RequestMethod.POST)
	void unlikeVideo(
			@PathVariable("id") long id,
			Principal p,
			HttpServletResponse rawResponse) throws IOException
	{
		Video video = null;
		try
		{
			video = findVideo(id);
		}
		catch(java.lang.Exception e)
		{
			rawResponse.sendError(404);
			return;
		}
		if(video == null)
		{
			rawResponse.sendError(404);
			return;
		}
		if(video.unlikeByUser(p.getName())){
			rawResponse.sendError(200);
			mVideos.save(video);
		}
		else{
			rawResponse.sendError(400);
		}
	}
	
	@RequestMapping(value="/video", method=RequestMethod.POST)
	@ResponseBody Video addVideo(@RequestBody Video video)
	{
		return mVideos.save(video);
	}
	@RequestMapping(value="/video", method=RequestMethod.GET)
	@ResponseBody Collection<Video> getAllVideos()
	{
		Collection<Video> result = new ArrayList<Video>();
		for (Video video: mVideos.findAll())
		{
			result.add(video);
		}
		return result;
	}
	
	@RequestMapping(value="video/search/findByName", method=RequestMethod.GET)
	@ResponseBody Collection<Video> GetVideosByName(
			@RequestParam("title") String title)
	{
		Collection<Video> result = mVideos.findByName(title);
		return result;
	}
	@RequestMapping(value="/video/{id}", method=RequestMethod.GET)
	@ResponseBody Video getVideo(
			@PathVariable("id") long id,
			HttpServletResponse rawResponse) throws IOException
	{
		Video video = findVideo(id);
		if(video == null)
		{
			rawResponse.sendError(404);	
		}
		return video;
	}
			
	@RequestMapping(value="/video/{id}/likedby", method=RequestMethod.GET)
	@ResponseBody Collection<String> likeVideo(
			@PathVariable("id") long id,
			HttpServletResponse rawResponse) throws IOException
	{
		Video video = findVideo(id);
		if(video == null)
		{
			rawResponse.sendError(404);
			return null;
		}
		return video.getLikeusers();
	}
	
	@RequestMapping(value="/video/search/findByDurationLessThan", method=RequestMethod.GET)
	@ResponseBody Collection<Video> GetVideosLessThan(
			@RequestParam("duration") long duration)
	{
		Collection<Video> result = mVideos.findByDurationLessThan(duration);
		return result;
	}
	
	
	
	private Video findVideo(long id)
	{
		return mVideos.findOne(id);
	}
}
