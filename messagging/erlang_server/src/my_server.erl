%%%-------------------------------------------------------------------
%%% @author GaS
%%% @copyright (C) 2016, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 09. Mar 2016 21:09
%%%-------------------------------------------------------------------
-module(my_server).
-author("GaS").
-include("amqp_client.hrl").
%% API
-export([start/0]).



start() ->
    {ok, _Connection} = amqp_connection:start(#amqp_params_network{}),
%    {ok, Channel} = amqp_connection:
    io:format("ciao"),
    ok.

