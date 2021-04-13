/* Import Packages */
const canvacord = require("canvacord");
const canvas = require("discord-canvas");
welcomeCanvas = new canvas.Welcome();
farewellCanvas = new canvas.Goodbye();
const DIG = require("discord-image-generation");
const cors = require("cors");
const express = require("express");
const app = express();

/* Import Custom Classes */
const AchievementEndpoint = require("./generate/achievement");
const ApprovedEndpoint = require("./generate/approved");
const ContrastEndpoint = require("./generate/contrast");
const FrameEndpoint = require("./generate/frame");
const InvertEndpoint = require("./generate/invert");
const RejectedEndpoint = require("./generate/rejected");
const BeLikeBillEndpoint = require("./generate/be-like-bill");

/* Initialize Middleware */
app.use(cors());

/* Functions */
const sendBuffer = (res, buffer) => {
  res.status(200);
  res.writeHead(200, {
    "Content-Type": "image/png",
    "Content-Length": buffer.length,
  });
  res.end(buffer);
};

const sendError = (res, err) => {
  console.log(err);
  res.status(500).json({
    message: err.message,
  });
};

/* Endpoints */
app.get("/rank-card", (req, res) => {
  const avatar =
    req.query.avatar || "https://cdn.discordapp.com/embed/avatars/0.png";
  const currentXp = req.query.currentxp || 0;
  const reqXp = req.query.reqxp || 100;
  const status = req.query.status || "idle"; // dnd || idle || offline || online || streaming
  const name = req.query.name || "Discord User";
  const discriminator = req.query.discriminator || "0000";
  const barColor = req.query.barcolor || "#FFFFFF";
  const fillType = req.query.filltype || "COLOR"; // COLOR || GRADIENT

  const card = new canvacord.Rank()
    .setAvatar(avatar)
    .setCurrentXP(currentXp)
    .setRequiredXP(reqXp)
    .setStatus(status)
    .setProgressBar(barColor, fillType)
    .setUsername(name)
    .setDiscriminator(discriminator);

  card
    .build()
    .then((buffer) => sendBuffer(res, buffer))
    .catch((err) => sendError(res, err));
});

app.get("/spotify-card", (req, res) => {
  const image =
    req.query.image || "https://cdn.discordapp.com/embed/avatars/0.png";
  const author = req.query.author;
  const album = req.query.album;
  const start = req.query.start;
  const end = req.query.end;
  const title = req.query.title;

  const card = new canvacord.Spotify()
    .setAuthor(author)
    .setAlbum(album)
    .setStartTimestamp(start)
    .setEndTimestamp(end)
    .setImage(image)
    .setTitle(title);

  card
    .build()
    .then((buffer) => sendBuffer(res, buffer))
    .catch((err) => sendError(res, err));
});

app.get("/welcome-card", async (req, res) => {
  const avatar =
    req.query.avatar || "https://cdn.discordapp.com/embed/avatars/0.png";
  const name = req.query.name || "Discord User";
  const discriminator = req.query.discriminator || "xxxx";
  const count = req.query.count || 0;
  const guild = req.query.guild || "Discord Server";

  // Background
  const bkg = req.query.bkg;

  // Text
  const message = req.query.message || "welcome to {server}";
  const member_count = req.query.member_count || "- {count}th member";

  try {
    const image = welcomeCanvas
      .setUsername(name)
      .setDiscriminator(discriminator)
      .setMemberCount(count)
      .setGuildName(guild)
      .setAvatar(avatar)
      .setText("title", "Welcome")
      .setText("message", message)
      .setText("member-count", member_count)
      .setColor("border", "#4D5E94")
      .setColor("username-box", "#4D5E94")
      .setColor("discriminator-box", "#4D5E94")
      .setColor("message-box", "#4D5E94")
      .setColor("title", "#4D5E94")
      .setColor("avatar", "#4D5E94");

    if (bkg) image.setBackground(bkg);

    const card = await image.toAttachment();
    sendBuffer(res, card.toBuffer());
  } catch (err) {
    sendError(res, err);
  }
});

app.get("/farewell-card", async (req, res) => {
  const avatar =
    req.query.avatar || "https://cdn.discordapp.com/embed/avatars/0.png";
  const name = req.query.name || "Discord User";
  const discriminator = req.query.discriminator || "xxxx";
  const count = req.query.count || 0;
  const guild = req.query.guild || "Discord Server";

  // Background
  const bkg = req.query.bkg;

  // Text
  const message = req.query.message || "Leaving from {server}";
  const member_count = req.query.member_count || "- {count}th member";

  try {
    const image = farewellCanvas
      .setUsername(name)
      .setDiscriminator(discriminator)
      .setMemberCount(count)
      .setGuildName(guild)
      .setAvatar(avatar)
      .setText("title", "Goodbye")
      .setText("message", message)
      .setText("member-count", member_count)
      .setColor("border", "#4D5E94")
      .setColor("username-box", "#4D5E94")
      .setColor("discriminator-box", "#4D5E94")
      .setColor("message-box", "#4D5E94")
      .setColor("title", "#4D5E94")
      .setColor("avatar", "#4D5E94");

    if (bkg) image.setBackground(bkg);

    const card = await image.toAttachment();
    sendBuffer(res, card.toBuffer());
  } catch (err) {
    sendError(res, err);
  }
});

app.get("/filters/:type", async (req, res) => {
  const type = req.params.type;
  const image = req.query.image;

  if (!image) {
    sendError(res, {
      message: "Missing query parameter 'image'",
    });
    return;
  }

  try {
    switch (type) {
      case "gay":
        data = await new DIG.Gay().getImage(image);
        break;

      case "greyscale":
        data = await new DIG.Greyscale().getImage(image);
        break;

      case "invert":
        data = await new DIG.Invert().getImage(image);
        break;

      case "sepia":
        data = await new DIG.Sepia().getImage(image);
        break;

      case "blur":
        data = await new DIG.Blur().getImage(image);
        break;

      case "contrast":
        result = await ContrastEndpoint(image);
        if (!result[0])
          sendError({
            message: result[1],
          });
        else data = result[1];
        break;

      case "invert":
        result = await InvertEndpoint(image);
        if (!result[0])
          sendError({
            message: result[1],
          });
        else data = result[1];
        break;

      default:
        sendError(res, {
          message: "Not a valid filter",
        });
        return;
    }

    if (data) sendBuffer(res, data);
  } catch (err) {
    sendError(res, err);
  }
});

const singleImageGen = new Set([
  "ad",
  "affect",
  "beautiful",
  "bobross",
  "confusedstonk",
  "delete",
  "discordblack",
  "discordblue",
  "facepalm",
  "hitler",
  "jail",
  "karaba",
  "mms",
  "notstonk",
  "poutine",
  "rip",
  "stonk",
  "tatoo",
  "thomas",
  "trash",
  "approved",
  "frame",
  "rejected",
]);

app.get("/generators/:type", async (req, res) => {
  const type = req.params.type;
  const image = req.query.image;
  const image1 = req.query.image1;
  const image2 = req.query.image2;
  const avatar1 = req.query.avatar1;
  const avatar2 = req.query.avatar2;
  const avatar3 = req.query.avatar3;
  const name1 = req.query.name1;
  const name2 = req.query.name2;
  const name3 = req.query.name3;
  const text = req.query.text;
  const currency = req.query.currency;

  try {
    if (singleImageGen.has(type.toLocaleLowerCase())) {
      if (!image) {
        sendError(res, {
          message: "Missing Query Parameter 'image'",
        });
      } else {
        switch (type) {
          case "ad":
            data = await new DIG.Ad().getImage(image);
            break;

          case "affect":
            data = await new DIG.Affect().getImage(image);
            break;

          case "beautiful":
            data = await new DIG.Beautiful().getImage(image);
            break;

          case "bobross":
            data = await new DIG.Bobross().getImage(image);
            break;

          case "confusedstonk":
            data = await new DIG.ConfusedStonk().getImage(image);
            break;

          case "delete":
            data = await new DIG.Delete().getImage(image);
            break;

          case "discordblack":
            data = await new DIG.DiscordBlack().getImage(image);
            break;

          case "discordblue":
            data = await new DIG.DiscordBlue().getImage(image);
            break;

          case "facepalm":
            data = await new DIG.Facepalm().getImage(image);
            break;

          case "hitler":
            data = await new DIG.Hitler().getImage(image);
            break;

          case "jail":
            data = await new DIG.Jail().getImage(image);
            break;

          case "karaba":
            data = await new DIG.Karaba().getImage(image);
            break;

          case "mms":
            data = await new DIG.Mms().getImage(image);
            break;

          case "notstonk":
            data = await new DIG.NotStonk().getImage(image);
            break;

          case "poutine":
            data = await new DIG.Poutine().getImage(image);
            break;

          case "rip":
            data = await new DIG.Rip().getImage(image);
            break;

          case "stonk":
            data = await new DIG.NotStonk().getImage(image);
            break;

          case "tatoo":
            data = await new DIG.Tatoo().getImage(image);
            break;

          case "thomas":
            data = await new DIG.Thomas().getImage(image);
            break;

          case "trash":
            data = await new DIG.Trash().getImage(image);
            break;

          case "approved":
            result = await ApprovedEndpoint(image);
            if (!result[0])
              sendError({
                message: result[1],
              });
            else data = result[1];
            break;

          case "rejected":
            result = await RejectedEndpoint(image);
            if (!result[0])
              sendError({
                message: result[1],
              });
            else data = result[1];
            break;

          case "frame":
            result = await FrameEndpoint(image);
            if (!result[0])
              sendError({
                message: result[1],
              });
            else data = result[1];
            break;
        }
        if (data) sendBuffer(res, data);
      }
    } else {
      switch (type) {
        case "batslap":
          if (!image1 || !image2) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image1' and 'image2'",
            });
            return;
          }
          data = await new DIG.Batslap().getImage(image1, image2);
          break;

        case "bed":
          if (!image1 || !image2) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image1' and 'image2'",
            });
            return;
          }
          data = await new DIG.Bed().getImage(image1, image2);
          break;

        case "doublestonk":
          if (!image1 || !image2) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image1' and 'image2'",
            });
            return;
          }
          data = await new DIG.DoubleStonk().getImage(image1, image2);
          break;

        case "kiss":
          if (!image1 || !image2) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image1' and 'image2'",
            });
            return;
          }
          data = await new DIG.Kiss().getImage(image1, image2);
          break;

        case "presentation":
          if (!text) {
            sendError(res, {
              message: "Missing query param 'text'",
            });
            return;
          }
          data = await new DIG.LisaPresentation().getImage(text);
          break;

        case "podium":
          if (!avatar1 || !avatar2 || !avatar3 || !name1 || !name2 || !name3) {
            sendError(res, {
              message:
                "Missing one or more of these query params: 'avatar1', 'avatar2', 'avatar3', 'name1', 'name2' and 'name3'",
            });
            return;
          }
          data = await new DIG.Podium().getImage(
            avatar1,
            avatar2,
            avatar3,
            name1,
            name2,
            name3
          );
          break;

        case "spank":
          if (!image1 || !image2) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image1' and 'image2'",
            });
            return;
          }
          data = await new DIG.Spank().getImage(image1, image2);
          break;

        case "wanted":
          if (!image || !currency) {
            sendError(res, {
              message:
                "Missing one or more query params: 'image' and 'currency'",
            });
            return;
          }
          data = await new DIG.Wanted().getImage(image, currency);
          break;

        case "achievement":
          if (!text) {
            sendError(res, {
              message: "Missing query param 'text'",
            });
            return;
          }
          data = await AchievementEndpoint(text);
          if (!data)
            sendError({
              message: "Text More Than 50 Chars!",
            });
          break;

        case "be-like-bill":
          if (!text) {
            sendError(res, {
              message: "Missing query param 'text'",
            });
            return;
          }
          data = await BeLikeBillEndpoint(text);
          if (!data)
            sendError({
              message: "Text More Than 50 Chars!",
            });
          break;

        default:
          sendError(res, {
            message: "Not a valid generator",
          });
          return;
      }
      if (data) sendBuffer(res, data);
    }
  } catch (err) {
    sendError(res, err);
  }
});

app.get("/gifs/:type", async (req, res) => {
  const type = req.params.type;
  const image = req.query.image;

  if (!image) {
    sendError(res, {
      message: "Missing query parameter 'image'",
    });
    return;
  }

  try {
    switch (type) {
      case "triggered":
        data = await new DIG.Triggered().getImage(image);
        break;

      default:
        sendError(res, {
          message: "Not a valid gif endpoint",
        });
        return;
    }

    if (data) sendBuffer(res, data);
  } catch (err) {
    sendError(res, err);
  }
});

app.get("/utils/:type", async (req, res) => {
  const type = req.params.type;
  try {
    switch (type) {
      case "circle":
        const image = req.query.image;
        if (!image) {
          sendError(res, {
            message: "Missing query parameter 'image'",
          });
          return;
        }
        data = await new DIG.Circle().getImage(image);
        break;

      case "color":
        const code = req.query.code;
        if (!code) {
          sendError(res, {
            message: "Missing query parameter 'code'",
          });
          return;
        }
        data = await new DIG.Color().getImage(code);
        break;

      default:
        sendError(res, {
          message: "Not a valid utility endpoint",
        });
    }

    if (data) sendBuffer(res, data);
  } catch (err) {
    sendError(res, err);
  }
});

app.all("*", (req, res) => {
  res.status(200).json({
    discord: "https://discord.gg/y4XMy6cJEU",
    endpoints: {
      filters: [
        "GET /filters/gay?image=url",
        "GET /filters/greyscale?image=url",
        "GET /filters/invert?image=url",
        "GET /filters/sepia?image=url",
        "GET /filters/blur?image=url",
        "GET /filters/contrast?image=url",
        "GET /filters/invert?image=url",
      ],
      generators: [
        "GET /generators/ad?image=url",
        "GET /generators/affect?image=url",
        "GET /generators/approved?image=url",
        "GET /generators/beautiful?image=url",
        "GET /generators/bobross?image=url",
        "GET /generators/confusedstonk?image=url",
        "GET /generators/delete?image=url",
        "GET /generators/discordblack?image=url",
        "GET /generators/discordblue?image=url",
        "GET /generators/facepalm?image=url",
        "GET /generators/frame?image=url",
        "GET /generators/hitler?image=url",
        "GET /generators/jail?image=url",
        "GET /generators/karaba?image=url",
        "GET /generators/mms?image=url",
        "GET /generators/notstonk?image=url",
        "GET /generators/poutine?image=url",
        "GET /generators/rejected?image=url",
        "GET /generators/rip?image=url",
        "GET /generators/stonk?image=url",
        "GET /generators/tatoo?image=url",
        "GET /generators/trash?image=url",
        "GET /generators/batslap?image1=url&image2=url",
        "GET /generators/bed?image1=url&image2=url",
        "GET /generators/doublestonk?image1=url&image2=url",
        "GET /generators/kiss?image1=url&image2=url",
        "GET /generators/spank?image1=url&image2=url",
        "GET /generators/presentation?text=Text",
        "GET /generators/achievement?text=Text",
        "GET /generators/be-like-bill?text=Text",
        "GET /generators/wanted?image=url&curreny=CurrencyCharacter",
        "GET /generators/podium?avatar1=url&avatar2=url&avatar3=url&name1=Text&name2=Text&name3=Text",
      ],
      gifs: ["GET /gifs/triggered?image=url"],
      utility: ["GET /utils/circle?image=url", "GET /utils/color?code=hexCode"],
      additional: [
        "GET /rank-card?avatar=url&currentxp=Number&reqxp=Number&status=[idle/dnd/online/streaming]&name=String",
        "GET /spotify-card?image=url&author=String&album=String&start=Date&end=Date&title=String",
        "GET /welcome-card?name=String&discriminator=String&count=Number&guild=String",
        "GET /farewell-card?name=String&discriminator=String&count=Number&guild=String",
      ],
    },
  });
});

app.listen(process.env.PORT || "3002", () => {
  console.log(`Now listening on ${process.env.PORT || "3002"}`);
});
