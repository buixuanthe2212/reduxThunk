import React, { Component } from 'react';
import { CheckBox } from 'react-native-elements';
import {
  Text,
  View,
  Image,
  TouchableOpacity,
  SafeAreaView,
  TextInput,
  StatusBar,
  TouchableWithoutFeedback,
  Keyboard,
} from 'react-native';
import { connect } from 'react-redux';
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import Spinner from 'react-native-loading-spinner-overlay';
import { Field, reduxForm } from 'redux-form';
import { UISubmitButton } from '../../../components/UI';

import { login, goToForgot } from '../actions';
import { styles } from './styles';
import { strings } from '../../../../locales/i18n';
import { ic_logo } from '../../../../assets';
import { required, maxLength } from '../../../helpers/ValidateForms';

type Props = {
  goToForgot: Function,
};

type State = {
  isRemember: boolean,
};

const renderField = ({
  label,
  keyboardType,
  returnKeyType,
  placeholder,
  isSecureTextEntry,
  value,
  meta: { touched, error, warning },
  input: { onChange, ...restInput },
}) => (
  // <View>
  <View style={styles.content_form}>
    <Text style={styles.form_text_title}>{label}</Text>
    <TextInput
      style={styles.form_text_input}
      placeholder={placeholder}
      returnKeyType={returnKeyType}
      keyboardType={keyboardType}
      secureTextEntry={isSecureTextEntry}
      autoCorrect={false}
      value={value}
      underlineColorAndroid="transparent"
      onChangeText={onChange}
      {...restInput}
    />
    {touched &&
      ((error && (
        <Text style={{ color: 'red', paddingTop: -30, paddingBottom: 10 }}>
          {error}
        </Text>
      )) ||
        (warning && <Text style={{ color: 'orange' }}>{warning}</Text>))}
  </View>
);

class LoginView extends Component<Props, State> {
  static navigationOptions = {
    header: null,
  };

  constructor(props) {
    super(props);

    this.state = {
      isRemember: false,
    };
  }

  componentDidMount = () => {};

  onPressLogin = values => {
    this.props.callLogin(
      values.company_id,
      values.employee_id,
      values.password,
      this.state.isRemember,
    );
  };

  render() {
    const { handleSubmit } = this.props;

    return (
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle="default" />
        <KeyboardAwareScrollView>
          <TouchableWithoutFeedback
            style={styles.container}
            onPress={Keyboard.dismiss}
          >
            <View style={styles.container}>
              <Spinner
                visible={this.props.stateIsLoading}
                textStyle={styles.textStyleSpinner}
              />
              <View style={styles.topContainer}>
                <Image style={styles.logo_icon} source={ic_logo} />
              </View>
              <View style={styles.contentContainer}>
                <Field
                  label={strings('login.company_id')}
                  component={renderField}
                  name="company_id"
                  placeholder={strings('login.company_id')}
                  keyboardType="default"
                  returnKeyType="next"
                  isSecureTextEntry={false}
                  validate={required}
                  warn={maxLength(6)}
                />
                <Field
                  label={strings('login.employee_id')}
                  component={renderField}
                  name="employee_id"
                  placeholder={strings('login.employee_id')}
                  keyboardType="default"
                  returnKeyType="next"
                  isSecureTextEntry={false}
                  validate={required}
                />
                <Field
                  label={strings('login.password')}
                  component={renderField}
                  name="password"
                  placeholder={strings('login.password')}
                  keyboardType="default"
                  returnKeyType="done"
                  isSecureTextEntry
                  validate={required}
                />
              </View>
              <View style={styles.bottomContainer}>
                <CheckBox
                  title={strings('login.remember')}
                  checked={this.state.isRemember}
                  checkedIcon="dot-circle-o"
                  uncheckedIcon="circle-o"
                  onPress={() => {
                    this.setState({ isRemember: !this.state.isRemember });
                  }}
                  containerStyle={styles.bottom_checkbox}
                />
                <UISubmitButton
                  title={strings('login.btn_login')}
                  onPress={handleSubmit(this.onPressLogin)}
                />
                <View style={styles.bottom_button_forgot}>
                  <TouchableOpacity
                    style={styles.bottom_button_forgot}
                    onPress={() => {
                      this.props.goToForgot();
                    }}
                  >
                    <Text style={styles.bottom_button_forgot_text}>
                      {strings('login.btn_forgot')}
                    </Text>
                  </TouchableOpacity>
                </View>
              </View>
              <View
                style={{
                  flex: 5,
                  alignContent: 'center',
                  justifyContent: 'center',
                  paddingTop: 50,
                  paddingBottom: 0,
                }}
              >
                <Text style={{ color: 'black', textAlign: 'center' }}>
                  ver 1.0.0
                </Text>
              </View>
            </View>
          </TouchableWithoutFeedback>
        </KeyboardAwareScrollView>
      </SafeAreaView>
    );
  }
}

const mapStatesToProps = state => ({
  stateIsLoggedIn: state.auth.isLoggedIn,
  stateIsLoading: state.auth.isLoading,
});

const mapDispatchToProps = dispatch => ({
  callLogin: (company_id, employee_id, password, isRemember) => {
    dispatch(login(company_id, employee_id, password, isRemember));
  },
  goToForgot() {
    dispatch(goToForgot());
  },
});

// const userStr

// get remember data
export const LoginScreen = connect(mapStatesToProps, mapDispatchToProps)(
  reduxForm({
    form: 'loginForm',
    initialValues: {
      company_id: '1',
      employee_id: '1',
      password: '123456',
    },
    enableReinitialize: true,
  })(LoginView),
);
