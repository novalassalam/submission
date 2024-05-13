import streamlit as st
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
sns.set(style='dark')

df = pd.read_csv('./dashboard/data.csv')
categorical_columns = ['season', 'yr', 'mnth', 'holiday', 'weekday', 'workingday', 'weathersit']
numerical_columns = ['temp', 'atemp', 'hum', 'windspeed', 'casual', 'registered', 'cnt']
df = df.replace({'season': {1: 'Spring', 2: 'Summer', 3: 'Fall', 4: 'Winter'}})
df = df.replace({'weathersit': {1: 'Clear', 2: 'Mist', 3: 'Light Snow', 4: 'Heavy Rain'}})
df = df.replace({'weekday': {0: 'Sunday', 1: 'Monday', 2: 'Tuesday', 3: 'Wednesday', 4: 'Thursday', 5: 'Friday', 6: 'Saturday'}})
df = df.replace({'mnth': {1: 'January', 2: 'February', 3: 'March', 4: 'April', 5: 'May', 6: 'June', 7: 'July', 8: 'August', 9: 'September', 10: 'October', 11: 'November', 12: 'December'}})
df = df.replace({'yr': {0 : 2011, 1 : 2012}})
df_categorical = df[categorical_columns]
df_numerical = df[numerical_columns]

def describe_and_histogram_numerical(df):
    st.subheader("Numerical Data")
    selected_col = st.selectbox("Select a column:", df_numerical.columns, key='distribution1')
    selected_data = df[selected_col]
    col1, col2 = st.columns([3,1])
    with col2:
        st.write(selected_data.describe())
    with col1:
        fig, ax = plt.subplots()
        sns.histplot(selected_data, ax=ax)
        st.pyplot(fig)

def countplot_categorical(df):
    st.subheader("Categorical Data")
    selected_cat = st.selectbox("Select a column:", df_categorical.columns, key='distribution2')
    fig, ax = plt.subplots(figsize=(12, 8))
    sns.countplot(x=selected_cat, data=df_categorical, ax=ax)
    st.pyplot(fig)
    
def line_daily_count(df):
    fig, ax = plt.subplots(figsize=(12, 8))
    sns.lineplot(data=df[['dteday', 'cnt']], x='dteday', y='cnt', ax=ax)
    ax.set_xticks(df['dteday'][::100])
    ax.set_xlabel('Date')
    ax.set_ylabel('Count')
    st.pyplot(fig)
    
def user_in_year(df):
    st.subheader('Demographic of type User in Year')
    col1, col2 = st.columns([1,1])
    with col1:
        selected_col_x = st.selectbox("Select X value:", ['season', 'mnth', 'holiday', 'weekday', 'workingday', 'weathersit'], key='box1')
    with col2:
        selected_col_y = st.selectbox("Select Y value:", ['cnt', 'casual', 'registered'], key='box2')
    fig, ax = plt.subplots(figsize=(12, 8))
    sns.barplot(x=selected_col_x, y=selected_col_y, hue='yr', data=df, ax=ax)
    ax.set_title(f'{selected_col_y} by {selected_col_x} in Year')
    st.pyplot(fig)
    
def piechart_season_cnt(df):
    fig, ax = plt.subplots(figsize=(5, 5))
    season_cnt = df.groupby('season')['cnt'].sum()
    explode = [0.1 if count == season_cnt.max() else 0 for count in season_cnt]
    ax.pie(season_cnt, labels=season_cnt.index, autopct='%1.1f%%', explode=explode, startangle=90)
    ax.set_title('Musim')
    st.pyplot(fig)
    
def piechart_weather_cnt(df):
    fig, ax = plt.subplots(figsize=(5, 5))
    season_cnt = df.groupby('weathersit')['cnt'].sum()
    explode = [0.1 if count == season_cnt.max() else 0 for count in season_cnt]
    ax.pie(season_cnt, labels=season_cnt.index, autopct='%1.1f%%', explode=explode, startangle=90)
    ax.set_title('Cuaca')
    st.pyplot(fig)

st.write('''
    # Project Analisis Data
            ''')

tab1, tab2 = st.tabs(['Dashboard', 'Distribusi Data'])

with st.container():
    with tab1:
        with st.container():
            col1, col2, col3 = st.columns([1,1,1])
            with col1:
                st.metric('Total User', value = df['cnt'].sum())
            with col2:
                st.metric('Registered User', value = df['registered'].sum())
            with col3:
                st.metric('Casual User', value = df['casual'].sum())
            line_daily_count(df)
            
            # Best Season and Weather
            st.subheader('Most rented bike based on Season & Weather')
            season, weather = st.columns([1,1])
            with season:
                piechart_season_cnt(df)
            with weather:
                piechart_weather_cnt(df)
            
            # User in Year
            user_in_year(df)
            
            
    with tab2:
        with st.container():
            st.write('## Histogram')
            describe_and_histogram_numerical(df_numerical)
            countplot_categorical(df_categorical)