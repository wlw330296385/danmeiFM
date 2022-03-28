import axios from '@/utils/api.request'

export const register = (data) => {
	return axios.post({
		url: `/public-api/register`,
		data: data
	})
}

export const login = (data) => {
	return axios.post({
		url: `/public-api/login`,
		data: data
	})
}

export const toResetPassword = (data) => {
	return axios.post({
		url: `/public-api/toResetPassword`,
		data: data
	})
}

export const toResetPassword2 = (data) => {
	return axios.post({
		url: `/public-api/toResetPassword2`,
		data: data
	})
}

export const logout = () => {
	return axios.post({
		url: `/logout`
	})
}

export const getUserInfo = () => {
	return axios.get({
		url: `/member/getUserInfo`
	})
}

export const getMyFollowList = (query) => {
	return axios.get({
		url: `/member/getMyFollowList`,
		data: query
	})
}

export const getMyHistoryList = (query) => {
	return axios.get({
		url: `/member/getMyHistoryList`,
		data: query
	})
}

export const getMyOrderList = (query) => {
	return axios.get({
		url: `/member/getMyOrderList`,
		data: query
	})
}

export const doFavorites = (data) => {
	return axios.post({
		url: `/member/doFavorites`,
		data
	})
}

export const updateMember = (data) => {
	return axios.post({
		url: `/member/updateMember`,
		data
	})
}

export const getMyMember = (query) => {
	return axios.get({
		url: `/agent/getMyMember`,
		data: query
	})
}
