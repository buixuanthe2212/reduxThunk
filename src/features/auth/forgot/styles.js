// @flow
import { StyleSheet } from 'react-native';
import { ColorPalette } from '../../../common/colors';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: ColorPalette.BG_CONTAINER,
  },
  container_inner: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  text_desc: {
    textAlign: 'center',
    lineHeight: 30,
  },
  view_form: {
    flexDirection: 'column',
    alignSelf: 'stretch',
  },
  text_forgot: {
    marginLeft: 20,
    marginRight: 20,
    marginTop: 40,
  },
  text_input: {
    marginTop: 10,
    marginLeft: 20,
    marginRight: 20,
    marginBottom: 20,
    padding: 8,
    backgroundColor: '#FFF',
    alignSelf: 'stretch',
  },
  view_buttons: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    alignSelf: 'stretch',
    height: 50,
  },

  btn_submit: {
    paddingTop: 10,
    paddingBottom: 10,
    paddingLeft: 30,
    paddingRight: 30,
    backgroundColor: '#6d77a2',
    color: '#fff',
    marginTop: 30,
  },
});
