import Vue from 'vue'
import Vuex from 'vuex'

import app from './module/app'
import user from './module/user.js';
import player from './module/player.js';

Vue.use(Vuex)

export default new Vuex.Store({
    state: {
        //
    },
    mutations: {
        //
    },
    actions: {
        //
    },
    modules: {
		user,
        app,
		player
    }
})
