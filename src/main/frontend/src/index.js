import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux'
import App from './components/App';
import imagesApp from './reducers'
import './index.css';
import { createStore, applyMiddleware } from 'redux';
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import promise from 'redux-promise'

const createStoreWithMiddleware = applyMiddleware(promise)(createStore)

ReactDOM.render(
  <Provider store={createStoreWithMiddleware(imagesApp)}>
      <BrowserRouter >
          <Switch>
              <Route path="/:sort" component={App} />
              <Route path="/" component={App} />
          </Switch>
      </BrowserRouter>
  </Provider>,
  document.getElementById('root')
);
