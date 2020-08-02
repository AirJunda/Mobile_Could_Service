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

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import retrofit.http.Query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;


// I WILL USE MONGODB INSTEAD OF JPA!!
@Controller
public class VideoController {
	// public class VideoSvc implements VideoSvcApi
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

	@Autowired
	public VideoRepository videoRepository;

	@RequestMapping(value="/go",method=RequestMethod.GET)
	public @ResponseBody String goodLuck(){
		return "Good Luck!";
	}


	/**
	 * Get request
	 **/
	//Returns the list of videos that have been added to the server as JSON. The list of videos should be persisted using Spring Data.
	// The list of Video objects should be able to be unmarshalled by the client into a Collection.
	//The return content-type should be application/json, which will be the default if you use @ResponseBody
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		//return (List<Video>)videoRepository.findAll();
		return Lists.newArrayList(videoRepository.findAll());
	}

	@RequestMapping(value = "/video/{id}", method = RequestMethod.GET)
	public @ResponseBody Video  getVideoById ( @PathVariable("id") long vid, HttpServletResponse response) throws IOException {
		//if( videoRepository.existsById(vid) == true ){
		//	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		//	return null;
		//}
		Video video = videoRepository.findById(vid);
		if (video == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return video;
	}
//
//
//	//POST /video
//
//	//The video metadata is provided as an application/json request body. The JSON should generate a valid instance of the Video class when deserialized by Spring's default Jackson library.
//	//Returns the JSON representation of the Video object that was stored along with any updates to that object made by the server.
//	//The server should store the Video in a Spring Data JPA repository. If done properly, the repository should handle generating ID's.
//	//A video should not have any likes when it is initially created.
//	//You will need to add one or more annotations to the Video object in order for it to be persisted with JPA.
//
	/*
	 * POST request
	 */
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) throws IOException {
		return saveVideo(v);
	}

	private Video saveVideo(Video video) {
		if (video != null){
			long vid = video.getId();
			//if (vid == 0){video.setId(1);}
			video.setLikes(0);
			return videoRepository.save(video);
		}
		return null;
	}

	// Helper functions. But not use in this version
	private String getDataUrl(long videoId){
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String base =
				"http://"+request.getServerName()
						+ ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		return base;
	}


	//POST /video/{id}/like
	//Allows a user to like a video. Returns 200 Ok on success, 404 if the video is not found, or 400 if the user has already liked the video.
	//The service should should keep track of which users have liked a video and prevent a user from liking a video twice. A POJO Video object is provided for you and you will need to annotate and/or add to it in order to make it persistable.
	//A user is only allowed to like a video once. If a user tries to like a video a second time, the operation should fail and return 400 Bad Request.
	@RequestMapping(value = "/video/{id}/like", method = RequestMethod.POST)
	public @ResponseBody void likeVideo( @PathVariable("id") long vid,
										 HttpServletResponse response,
										 Principal p) {
		Video video = videoRepository.findById(vid);
		if (video == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String username = p.getName();
		Set<String> likeby = video.getLikedBy();
		if (likeby.contains(username)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		video.setLikes(video.getLikes() + 1);
		likeby.add(username);
		video.setLikedBy(likeby);
		Video tmp = videoRepository.save(video);
		response.setStatus(HttpServletResponse.SC_OK);
		return;
	}


	/*
	POST /video/{id}/unlike
	Allows a user to unlike a video that he/she previously liked.
	Returns 200 OK on success, 404 if the video is not found,
	and a 400 if the user has not previously liked the specified video.
    */
	@RequestMapping(value = "/video/{id}/unlike", method = RequestMethod.POST)
	public @ResponseBody void dislikeVideo( @PathVariable("id") long vid,
										 HttpServletResponse response,
										 Principal p) {
		Video video = videoRepository.findById(vid);
		if (video == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String username = p.getName();
		Set<String> likeby = video.getLikedBy();
		if (!likeby.contains(username)){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		video.setLikes(video.getLikes() - 1);
		likeby.remove(username);
		video.setLikedBy(likeby);
		Video tmp = videoRepository.save(video);
		response.setStatus(HttpServletResponse.SC_OK);
		return;
	}



	/*
	GET /video/{id}/likedby
	Returns a list of the string usernames of the users that have liked the specified video.
	If the video is not found, a 404 error should be generated.
	*/
	@RequestMapping(value = "/video/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody
	Set<String> getVideoLikeBy(@PathVariable("id") long vid,
											  HttpServletResponse response){
		Video v = videoRepository.findById(vid);
		if (v == null){
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		Set<String> likesBy = v.getLikedBy();
		return likesBy;
	}



	/*GET
	/video/search/findByName?title={title}
	Returns a list of videos whose titles match the given parameter or an empty list if none are found.
	*/
	@RequestMapping(value = "/video/search/findByName", method = RequestMethod.GET)
	public @ResponseBody
	List<Video> findByTitle( @RequestParam("title") String title) {
		List<Video> vds = videoRepository.findByName(title);
		if (vds == null){
			return Lists.newArrayList();
		}else{
			return vds;
		}
	}

	/*
	GET /video/search/findByDurationLessThan?duration={duration}
	Returns a list of videos whose durations are less than the given parameter or an empty list if none are found.
	*/
	@RequestMapping(value = "/video/search/findByDurationLessThan", method = RequestMethod.GET)
	public @ResponseBody
	List<Video> findByLessthanDur( @RequestParam("duration") Long duration) {
		List<Video> vds = videoRepository.findByDurationLessThan(duration);
		if (vds == null){
			return Lists.newArrayList();
		}else{
			return vds;
		}
	}


}
