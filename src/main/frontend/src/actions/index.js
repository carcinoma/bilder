import axios from 'axios';
import {apiBase} from '../constants'

export const addImage = image => {
  return {
    type: 'ADD_IMAGE',
    image
  }
}

export function fetchImages(id) {

    let request;
    if(!id) {
        request = axios.get(apiBase + 'api/tree.json');
    } else if(id === 'date') {
        request = axios.get(apiBase + 'api/by-date.json');
    } else if(id === 'camera') {
        request = axios.get(apiBase + 'api/by-cammodel.json');
    } else if(id === 'tags') {
        request = axios.get(apiBase + 'api/tags');
    }

    return {
        type: "FETCH_IMAGES",
        payload: request
    }
}


export function loading() {

    return {
        type: "LOADING",
        payload: null
    }

}