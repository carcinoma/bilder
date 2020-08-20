const images = (state = null, action) => {
  switch (action.type) {
    case 'ADD_IMAGE':
      return [
        ...state,
        {
          id: action.id,
          text: action.text,
          completed: false
        }
      ]
    case "FETCH_IMAGES":
        return action.payload.data;
    case "LOADING":
        return null;
    default:
      return state
  }
}

export default images