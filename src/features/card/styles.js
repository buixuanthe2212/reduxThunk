// @flow
import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  toolbar: {
    height: 48,
    backgroundColor: 'blue',
    flexDirection: 'row',
  },
  icon: {
    marginLeft: 30,
    marginRight: 20,
    width: 24,
    height: 24,
    resizeMode: 'contain',
  },
  text: {
    fontSize: 16,
  },
  button: {
    margin: 20,
    height: 40,
    paddingBottom: 5
  }
});
