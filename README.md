# プロジェクトラーニング　後半プロジェクト

## 学食スワローズ サーバサイドプログラム

### ディレクトリ構成

```zsh
.
├── bin (.gitignore)
│
├── lib
│   └── postgresql-42.7.3.jar
└── src
    ├── main
    │   ├── Main.java
    │   ├── server
    │   │   ├── CommandExecutor.java
    │   │   └── Server.java
    │   └── util
    │       └── PropertyUtil.java
    ├── properties
    │   └── common.properties
    └── test
        └── TestClient.java
```

- `bin` : コンパイルされたクラスファイルが格納されるディレクトリ
- `lib` : JDBC ドライバのような jar ファイルを格納されるディレクトリ
- `src/main/server` : サーバプログラムのソースコードが格納されるディレクトリ
- `src/properties` : ポート番号や IP アドレス、データベース用の変数などの共通の変数を格納するディレクトリ
- `src/main/util` : プロパティファイルを読み込むクラスが格納されるディレクトリ
- `src/test` : サーバ側でテスト用に使うクライアントプログラムが格納されるディレクトリ

### プログラム概要

クライアントからのリクエストを受け取り、データベース操作を行うサーバプログラムです。

クライアントのリクエストは、`cmd args`という形式で送信されます。
`cmd`はリクエストの種類を示す文字列で、`args`はリクエストに必要な引数を表します。

たとえば、`login user1 pass1`というリクエストを送信すると、ユーザ名が`user1`、パスワードが`pass1`のユーザがデータベースに存在するかどうかを確認します（ここでの"login"が`cmd`, "user1 pass1"が`args`）。

<details open>
<summary> クライアントプログラムの例
</summary>

```Java
import java.io.*;
import java.net.Socket;


public class Client {

    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String[] args) {

        Client client = new Client();
        client.start();
    }

    // クライアントを起動
    public void start() {
        System.out.println("Client started on port " + PORT);
        try {
            clientSocket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // リクエスト送信
            out.println("login user1 pass1");

            // レスポンス受信
            System.out.println("Received: " + in.readLine());

        } catch (Exception e) {
            ~~~
        }
    }

}
```

</details>

### サーバプログラムのアルゴリズム

1. サーバプログラムは、`Server`クラスの`main`メソッドでクライアントの接続待ちを行います。
2. 接続が確立されると、`Server`クラスの`handleClient()`メソッドが呼び出され、クライアントからのリクエストを解析します。(マルチスレッドで処理)
3. `handleClient()`メソッドでは、クライアントのリクエストを`cmd`と`args`に分解します。
4. 得られた`cmd`に応じて、適切な`CommandExecutor`インターフェースの`execute()`メソッドを実行します。

<!-- <img src="images/server.drawio.svg" alt="" width="70%"> -->
