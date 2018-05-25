// @flow
import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    // alignItems: 'center',
    justifyContent: 'center',
    padding: 10,
    backgroundColor: '#e8eaf2', // '#e8eaf2',
  },
  topContainer: {
    flex: 20,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 30,
    paddingBottom: 30,
  },
  contentContainer: {
    flex: 60,
    flexDirection: 'column',
    paddingTop: 0,
    paddingBottom: 0,
  },
  bottomContainer: {
    flex: 15,
    paddingTop: 0,
    paddingBottom: 0,
  },

  // top
  top_logo_icon: {
    width: 155,
    height: 70,
    resizeMode: 'contain',
  },
  top_logo_title: {
    color: '#020202',
    fontSize: 18,
    fontWeight: 'bold',
    textAlign: 'center',
    opacity: 1,
  },

  // content
  content_form: {
    flex: 1,
    alignItems: 'stretch',
    flexDirection: 'column',
    margin: 0,
  },
  form_text_title: {
    fontWeight: 'bold',
    marginBottom: 10,
  },
  form_text_input: {
    height: 40,
    backgroundColor: '#fff',
    color: 'black',
    marginBottom: 20,
    paddingHorizontal: 10,
    borderColor: '#fff',
    borderRadius: 4,
    shadowColor: '#e8eaf2',
    shadowOffset: {
      width: 1,
      height: 3,
    },
    shadowRadius: 5,
    shadowOpacity: 1.0,
  },

  // bottom
  bottom_button_login: {
    borderRadius: 5,
    height: 40,
    marginBottom: 10,
    marginLeft: -10,
    marginRight: -10,
  },
  bottom_button_login_text: {
    color: '#fff',
    fontWeight: 'bold',
    fontSize: 18,
  },
  bottom_checkbox: {
    backgroundColor: 'transparent',
    height: 45,
    marginBottom: 0,
    marginLeft: 0,
    marginRight: 0,
  },
  bottom_view_button_forgot: {
    flexDirection: 'column',
    alignItems: 'flex-end',
    height: 70,
    marginRight: 0,
  },

  bottom_button_forgot: {
    flex: 1,
    marginTop: 15,
    paddingRight: 0,
  },
  bottom_button_forgot_text: {
    textAlign: 'right',
    color: 'rgb(32, 53, 70)',
    fontWeight: 'bold',
    textDecorationLine: 'underline',
  },

  textStyleSpinner: {
    color: '#999999',
  },
});
