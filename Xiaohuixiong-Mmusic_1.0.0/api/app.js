import axios from '@/utils/api.request'

export const getAppConfig = () => {
	return axios.get({
		url: `/public-api/getAppConfig`
	})
}
