import axios from '@/utils/api.request'

export const getAlbumDetails = (query) => {
	return axios.get({
		url: `/public-api/getAlbumDetails`,
		data: query
	})
}

export const getAlbumSingleDetails = (query) => {
	return axios.get({
		url: `/public-api/getAlbumSingleDetails`,
		data: query
	})
}

export const getAlbumSingleBarrage = (query) => {
	return axios.get({
		url: `/public-api/getAlbumSingleBarrage`,
		data: query
	})
}

export const addSingleBarrage = (data) => {
	return axios.post({
		url: `/public-api/addSingleBarrage`,
		data: data
	})
}

export const getMusicList = (query) => {
	return axios.get({
		url: `/public-api/getMusicList`,
		data: query
	})
}

export const getAnimeList = (query) => {
	return axios.get({
		url: `/public-api/getAnimeList`,
		data: query
	})
}

export const getRadioDramaList = (query) => {
	return axios.get({
		url: `/public-api/getRadioDramaList`,
		data: query
	})
}

export const getRadioDramaRecommendList = (query) => {
	return axios.get({
		url: `/public-api/getRadioDramaRecommendList`,
		data: query
	})
}


export const addMemberPlayHistory = (data) => {
	return axios.post({
		url: `/member/addMemberPlayHistory`,
		data
	})
}

export const addMemberAlbum = (data) => {
	return axios.post({
		url: `/member/addMemberAlbum`,
		data
	})
}

export const getMyAlbum = (data) => {
	return axios.get({
		url: `/member/getMyAlbum`,
		data
	})
}

export const addMemberSingle = (data) => {
	return axios.post({
		url: `/member/addMemberSingle`,
		data
	})
}

export const delMyAlbum = (query) => {
	return axios.delete({
		url: `/member/delMyAlbum`,
		data: query
	})
}

export const getMySingle = (data) => {
	return axios.get({
		url: `/member/getMySingle`,
		data
	})
}

export const addPlayTimes = (data) => {
	return axios.post({
		url: `/public-api/works/addPlayTimes`,
		data
	})
}
